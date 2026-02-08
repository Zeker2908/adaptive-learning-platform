package ru.zeker.authentication.service;

import io.jsonwebtoken.ExpiredJwtException;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import ru.zeker.authentication.domain.dto.Tokens;
import ru.zeker.authentication.domain.dto.request.ConfirmationEmailRequest;
import ru.zeker.authentication.domain.dto.request.ForgotPasswordRequest;
import ru.zeker.authentication.domain.dto.request.LoginRequest;
import ru.zeker.authentication.domain.dto.request.RegisterRequest;
import ru.zeker.authentication.domain.dto.request.ResendVerificationRequest;
import ru.zeker.authentication.domain.dto.request.ResetPasswordRequest;
import ru.zeker.authentication.domain.mapper.UserMapper;
import ru.zeker.authentication.domain.model.entity.LocalAuth;
import ru.zeker.authentication.domain.model.entity.User;
import ru.zeker.authentication.domain.model.enums.Role;
import ru.zeker.authentication.exception.InvalidTokenException;
import ru.zeker.authentication.exception.TooManyRequestsException;
import ru.zeker.authentication.exception.UserAlreadyEnableException;
import ru.zeker.common.dto.kafka.smtp.EmailEvent;
import ru.zeker.common.dto.kafka.smtp.EmailEventType;
import ru.zeker.common.exception.ErrorCode;
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
        var email = request.getEmail().toLowerCase();
        log.info("Registering new user with email: {}", email);

        var user = userMapper.toEntity(request, passwordEncoder);

        userService.create(user);
        log.debug("User created in database: {}", email);

        passwordHistoryService.create(user, request.getPassword());
        log.debug("Password history created in database");

        var token = jwtService.generateEmailToken(user);
        var userRegisteredEvent = createEmailEvent(user,
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
        var email = request.getEmail().toLowerCase();
        log.info("Login attempt for user: {}", email);

        var authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                        email,
                        request.getPassword()
                )
        );

        var user = (User) authentication.getPrincipal();

        log.debug("Authentication successful for user: {}", email);

        var jwtToken = jwtService.generateAccessToken(user);
        var refreshToken = refreshTokenService.createRefreshToken(user);

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

        var token = refreshTokenService.verifyRefreshToken(refreshToken);
        var claims = jwtUtils.parseClaimsJws(refreshToken);
        var user = new User();
        user.setId(UUID.fromString(jwtUtils.getUserId(claims)));
        user.setEmail(jwtUtils.getUsername(claims));
        user.setRole(Role.fromString(jwtUtils.getRole(claims)));

        var jwtToken = jwtService.generateAccessToken(user);
        var newRefreshToken = refreshTokenService.rotateRefreshToken(token, user);

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
    public Tokens confirmEmail(ConfirmationEmailRequest request) {
        try {
            log.info("Email confirmation request");
            var token = request.getToken();

            var user = userService.findById(UUID.fromString(jwtUtils.extractUserId(token)));
            if (!jwtService.isTokenValid(token, user)) {
                log.warn("Attempt to confirm email with invalid token");
                throw new InvalidTokenException("Email confirmation token invalid", ErrorCode.INVALID_EMAIL_TOKEN);
            }

            if (user.isEnabled()) {
                log.warn("Attempt to re-confirm already activated account: {}", user.getEmail());
                throw new UserAlreadyEnableException();
            }

            user.getLocalAuth().setEnabled(true);
            userService.update(user);

            log.info("Email successfully confirmed for user: {}", user.getEmail());
            var jwtToken = jwtService.generateAccessToken(user);
            var refreshToken = refreshTokenService.createRefreshToken(user);

            log.info("User successfully logged in: {}", user.getEmail());

            return Tokens.builder()
                    .token(jwtToken)
                    .refreshToken(refreshToken)
                    .build();
        } catch (ExpiredJwtException e) {
            throw new InvalidTokenException("Email confirmation token expired", ErrorCode.EMAIL_TOKEN_EXPIRED);
        }
    }

    /**
     * Process password recovery request.
     * Sends email with password reset instructions
     *
     * @param request request with user email
     */
    public void forgotPassword(ForgotPasswordRequest request) {
        var email = request.getEmail().toLowerCase();
        log.info("Password recovery request for: {}", email);

        var user = userService.findByEmail(email);
        var token = jwtService.generateEmailToken(user);

        var event = createEmailEvent(user,
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
        try {
            log.info("Password reset request");
            var token = request.getToken();
            var password = request.getPassword();
            var encodedPassword = passwordEncoder.encode(password);
            var user = userService.findById(UUID.fromString(jwtUtils.extractUserId(token)));

            // Combined token validation
            if (jwtUtils.isTokenExpired(token) ||
                    !user.getVersion().equals(jwtUtils.extractVersion(token)) ||
                    !jwtUtils.isValidUsername(token, user.getEmail())) {
                log.warn("Invalid token for password reset");
                throw new InvalidTokenException("Invalid token for password reset", ErrorCode.INVALID_EMAIL_TOKEN);
            }

            var localAuth = Optional.ofNullable(user.getLocalAuth())
                    .orElseGet(() -> {
                        var localAuthNew = LocalAuth.builder()
                                .user(user)
                                .enabled(true)
                                .build();
                        user.setLocalAuth(localAuthNew);
                        return localAuthNew;
                    });
            localAuth.setPassword(encodedPassword);

            passwordHistoryService.create(user, password);
            userService.update(user);
            refreshTokenService.revokeAllUserTokens(user.getId());

            log.info("Password successfully reset for user: {}", user.getEmail());
        } catch (ExpiredJwtException e) {
            throw new InvalidTokenException("Password reset token expired", ErrorCode.EMAIL_TOKEN_EXPIRED);
        }
    }

    /**
     * Resends verification email to the specified user if they are not yet verified
     * and if the cooldown period has expired.
     *
     * @param request contains the email address for resending verification
     * @throws UserAlreadyEnableException if user is already verified
     * @throws TooManyRequestsException   if email was requested too recently
     */
    public void resendVerificationEmail(ResendVerificationRequest request) {
        log.info("Resend verification email request");
        var email = request.getEmail().toLowerCase();
        var user = userService.findByEmail(email);

        if (user.isEnabled()) {
            log.warn("User already verified: {}", email);
            throw new UserAlreadyEnableException();
        }

        if (!verificationCooldownService.canResendEmail(email)) {
            log.warn("Attempt to resend verification email too frequently: {}", email);
            throw new TooManyRequestsException();
        }

        var token = jwtService.generateEmailToken(user);
        var event = createEmailEvent(user,
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
