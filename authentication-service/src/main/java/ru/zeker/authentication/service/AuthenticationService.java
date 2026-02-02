package ru.zeker.authentication.service;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import ru.zeker.authentication.domain.dto.Tokens;
import ru.zeker.authentication.domain.dto.request.ConfirmationEmailRequest;
import ru.zeker.authentication.domain.dto.request.LoginRequest;
import ru.zeker.authentication.domain.dto.request.RegisterRequest;
import ru.zeker.authentication.domain.dto.request.ResendVerificationRequest;
import ru.zeker.authentication.domain.dto.request.ResetPasswordRequest;
import ru.zeker.authentication.domain.dto.request.UserUpdateRequest;
import ru.zeker.authentication.domain.mapper.UserMapper;
import ru.zeker.authentication.domain.model.entity.LocalAuth;
import ru.zeker.authentication.domain.model.entity.RefreshToken;
import ru.zeker.authentication.domain.model.entity.User;
import ru.zeker.authentication.exception.InvalidTokenException;
import ru.zeker.authentication.exception.TooManyRequestsException;
import ru.zeker.authentication.exception.UserAlreadyEnableException;
import ru.zeker.common.dto.kafka.smtp.EmailEvent;
import ru.zeker.common.dto.kafka.smtp.EmailEventType;
import ru.zeker.common.util.JwtUtils;

import java.util.Map;
import java.util.Optional;
import java.util.UUID;
/**
 * Service for managing user authentication and registration
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class AuthenticationService {
    private final UserService userService;
    private final UserMapper userMapper;
    private final JwtService jwtService;
    private final JwtUtils jwtUtils;
    private final PasswordEncoder passwordEncoder;
    private final AuthenticationManager authenticationManager;
    private final RefreshTokenService refreshTokenService;
    private final KafkaProducer kafkaProducer;
    private final PasswordHistoryService passwordHistoryService;
    private final VerificationCooldownService verificationCooldownService;

    /**
     * Register a new user and send email verification message
     *
     * @param request new user data
     */
    @Transactional
    public void register(RegisterRequest request) {
        String email = request.getEmail().toLowerCase();
        log.info("Registering new user with email: {}", email);

        User user = userMapper.toEntity(request, passwordEncoder);

        userService.create(user);
        log.debug("User created in database: {}", email);

        passwordHistoryService.create(user, request.getPassword());
        log.debug("Password history created in database");

        String token = jwtService.generateEmailToken(user);
        EmailEvent userRegisteredEvent = createEmailEvent(user,
                EmailEventType.EMAIL_VERIFICATION,
                Map.of("token", token));

        kafkaProducer.sendEmailEvent(userRegisteredEvent);
        log.info("Email verification message sent: {}", email);
    }

    /**
     * Authenticate user and issue tokens
     *
     * @param request login data
     * @return object with JWT and refresh tokens
     */
    public Tokens login(LoginRequest request) {
        String email = request.getEmail().toLowerCase();
        log.info("Login attempt for user: {}", email);

        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                        email,
                        request.getPassword()
                )
        );

        User user = (User) authentication.getPrincipal();

        log.debug("Authentication successful for user: {}", email);

        String jwtToken = jwtService.generateAccessToken(user);
        String refreshToken = refreshTokenService.createRefreshToken(user);

        log.info("User successfully logged in: {}", email);

        return Tokens.builder()
                .token(jwtToken)
                .refreshToken(refreshToken)
                .build();
    }

    /**
     * Refresh JWT token using refresh token
     *
     * @param refreshToken refresh token
     * @return new set of tokens
     */
    public Tokens refreshToken(String refreshToken) {
        log.debug("Token refresh request");

        RefreshToken token = refreshTokenService.verifyRefreshToken(refreshToken);
        User user = userService.findById(token.getUserId());

        String jwtToken = jwtService.generateAccessToken(user);
        String newRefreshToken = refreshTokenService.rotateRefreshToken(token);

        log.debug("Tokens successfully refreshed for user: {}", user.getEmail());

        return Tokens.builder()
                .token(jwtToken)
                .refreshToken(newRefreshToken)
                .build();
    }

    /**
     * Confirm user email
     *
     * @param request request with JWT token for confirmation
     * @throws InvalidTokenException      if token is invalid
     * @throws UserAlreadyEnableException if email is already confirmed
     */
    public void confirmEmail(ConfirmationEmailRequest request) {
        log.info("Email confirmation request");
        String token = request.getToken();

        User user = userService.findById(jwtService.extractUserId(token));

        if (!jwtService.isTokenValid(token, user)) {
            log.warn("Attempt to confirm email with invalid token");
            throw new InvalidTokenException();
        }

        if (user.isEnabled()) {
            log.warn("Attempt to re-confirm already activated account: {}", user.getEmail());
            throw new UserAlreadyEnableException();
        }

        user.getLocalAuth().setEnabled(true);
        userService.update(user);

        log.info("Email successfully confirmed for user: {}", user.getEmail());
    }

    /**
     * Process password recovery request.
     * Sends email with password reset instructions
     *
     * @param request request with user email
     */
    public void forgotPassword(UserUpdateRequest request) {
        String email = request.getEmail().toLowerCase();
        log.info("Password recovery request for: {}", email);

        User user = userService.findByEmail(email);
        String token = jwtService.generateEmailToken(user);

        EmailEvent event = createEmailEvent(user,
                EmailEventType.FORGOT_PASSWORD,
                Map.of("token", token));

        kafkaProducer.sendEmailEvent(event);
        log.info("Password recovery email sent to: {}", email);
    }

    /**
     * Reset user password using token
     *
     * @param request request with new password
     * @throws InvalidTokenException if token is invalid
     */
    @Transactional
    public void resetPassword(ResetPasswordRequest request) {
        log.info("Password reset request");
        String token = request.getToken();
        String password = request.getPassword();
        String encodedPassword = passwordEncoder.encode(password);

        // Combined token validation
        if (jwtUtils.isTokenExpired(token) ||
                !userService.findById(jwtService.extractUserId(token)).getVersion().equals(jwtService.extractVersion(token)) ||
                !jwtUtils.isValidUsername(token, userService.findById(jwtService.extractUserId(token)).getEmail())) {
            log.warn("Invalid token for password reset");
            throw new InvalidTokenException();
        }

        User user = userService.findById(jwtService.extractUserId(token));

        LocalAuth localAuth = Optional.ofNullable(user.getLocalAuth())
                .orElseGet(() -> {
                    LocalAuth localAuthNew = LocalAuth.builder()
                            .user(user)
                            .enabled(true)
                            .build();
                    user.setLocalAuth(localAuthNew);
                    return localAuthNew;
                });
        localAuth.setPassword(encodedPassword);

        passwordHistoryService.create(user, password);
        userService.update(user);
        refreshTokenService.revokeAllUserTokens(token);

        log.info("Password successfully reset for user: {}", user.getEmail());
    }

    /**
     * Resends verification email to the specified user if they are not yet verified
     * and if the cooldown period has expired.
     * @param request contains the email address for resending verification
     *
     * @throws UserAlreadyEnableException if user is already verified
     * @throws TooManyRequestsException   if email was requested too recently
     */
    public void resendVerificationEmail(ResendVerificationRequest request) {
        log.info("Resend verification email request");
        String email = request.getEmail().toLowerCase();
        User user = userService.findByEmail(email);

        if (user.isEnabled()) {
            log.warn("User already verified: {}", email);
            throw new UserAlreadyEnableException();
        }

        if (!verificationCooldownService.canResendEmail(email)) {
            log.warn("Attempt to resend verification email too frequently: {}", email);
            throw new TooManyRequestsException();
        }

        String token = jwtService.generateEmailToken(user);
        EmailEvent event = createEmailEvent(user,
                EmailEventType.EMAIL_VERIFICATION,
                Map.of("token", token));

        kafkaProducer.sendEmailEvent(event);
        verificationCooldownService.updateCooldown(email);
        log.info("Verification email sent to: {}", email);
    }

    private EmailEvent createEmailEvent(User user, EmailEventType type, Map<String, String> data) {
        return EmailEvent.builder()
                .type(type)
                .id(UUID.randomUUID().toString())
                .email(user.getEmail())
                .payload(data)
                .build();
    }
}
