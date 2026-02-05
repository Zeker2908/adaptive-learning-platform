package ru.zeker.authentication.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.ws.rs.core.HttpHeaders;
import lombok.RequiredArgsConstructor;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.CookieValue;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import ru.zeker.authentication.domain.dto.request.ConfirmationEmailRequest;
import ru.zeker.authentication.domain.dto.request.LoginRequest;
import ru.zeker.authentication.domain.dto.request.RegisterRequest;
import ru.zeker.authentication.domain.dto.request.ResendVerificationRequest;
import ru.zeker.authentication.domain.dto.request.ResetPasswordRequest;
import ru.zeker.authentication.domain.dto.request.UserUpdateRequest;
import ru.zeker.authentication.domain.dto.response.AuthenticationResponse;
import ru.zeker.authentication.exception.TokenExpiredException;
import ru.zeker.authentication.service.AuthenticationService;
import ru.zeker.authentication.service.RefreshTokenService;
import ru.zeker.authentication.util.CookieUtils;

import java.time.Duration;

/**
 * Controller for managing user authentication and authorization.
 * Provides registration, login, access token management,
 * password recovery, and email confirmation.
 */
@Validated
@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
@Tag(name = "Authentication", description = "User management: registration, login, email confirmation, and password recovery")
public class AuthenticationController {
    private final AuthenticationService authenticationService;
    private final RefreshTokenService refreshTokenService;

    @Value("${jwt.refresh.expiration}")
    private long durationDays;

    /**
     * Registers a new user and sends a confirmation email.
     *
     * @param request {@link RegisterRequest} - registration data
     * @return {@link ResponseEntity} with HTTP status 201 (Created)
     * @throws jakarta.validation.ConstraintViolationException if the request data is invalid
     */
    @Operation(summary = "Register a new user", description = "Creates a user and sends a confirmation email")
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "User successfully registered"),
            @ApiResponse(responseCode = "409", description = "User already exists")
    })
    @PostMapping("/register")
    public ResponseEntity<Void> signup(@RequestBody @Valid RegisterRequest request) {
        authenticationService.register(request);
        return ResponseEntity.status(HttpStatus.CREATED).build();
    }

    /**
     * Authenticates a user and issues access tokens.
     *
     * @param request  {@link LoginRequest} - user credentials
     * @param response {@link HttpServletResponse} to set refresh token in cookie
     * @return {@link ResponseEntity} with {@link AuthenticationResponse} (access token)
     * @throws jakarta.validation.ConstraintViolationException                     if request data is invalid
     * @throws org.springframework.security.authentication.BadCredentialsException if credentials are incorrect
     */
    @Operation(summary = "Login", description = "Authenticates user and sets refresh token in cookie")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Authentication successful",
                    content = @Content(schema = @Schema(implementation = AuthenticationResponse.class))),
            @ApiResponse(responseCode = "401", description = "Invalid credentials")
    })
    @PostMapping("/login")
    public ResponseEntity<AuthenticationResponse> login(
            @RequestBody @Valid LoginRequest request,
            HttpServletResponse response) {
        var tokens = authenticationService.login(request);
        var cookie = CookieUtils.createTokenCookie(tokens.getRefreshToken(), Duration.ofMillis(durationDays));
        response.addHeader(HttpHeaders.SET_COOKIE, cookie.toString());
        return ResponseEntity.ok(new AuthenticationResponse(tokens.getToken()));
    }

    /**
     * Confirms user email using confirmation token.
     *
     * @param request {@link ConfirmationEmailRequest} - confirmation token
     * @return {@link ResponseEntity} with {@link AuthenticationResponse} (access token)
     * @throws jakarta.validation.ConstraintViolationException if token is invalid
     * @throws TokenExpiredException                           if token is expired
     */
    @Operation(summary = "Email confirmation", description = "Confirms email using provided token")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Email confirmed successful",
                    content = @Content(schema = @Schema(implementation = AuthenticationResponse.class))),
            @ApiResponse(responseCode = "400", description = "Invalid token")
    })
    @PatchMapping("/email/verify")
    public ResponseEntity<AuthenticationResponse> confirmEmail(@RequestBody @Valid ConfirmationEmailRequest request,
                                                               HttpServletResponse response) {
        var tokens = authenticationService.confirmEmail(request);
        var cookie = CookieUtils.createTokenCookie(tokens.getRefreshToken(), Duration.ofMillis(durationDays));
        response.addHeader(HttpHeaders.SET_COOKIE, cookie.toString());
        return ResponseEntity.ok(new AuthenticationResponse(tokens.getToken()));
    }

    /**
     * Resends confirmation email.
     *
     * @param request {@link ResendVerificationRequest} - user email
     * @return {@link ResponseEntity} with HTTP status 202 (Accepted)
     * @throws jakarta.validation.ConstraintViolationException if email is invalid
     */
    @Operation(summary = "Resend confirmation", description = "Sends confirmation email if not already confirmed")
    @ApiResponses({
            @ApiResponse(responseCode = "202", description = "Email sent"),
            @ApiResponse(responseCode = "429", description = "Email already sent, retry after 60 seconds")
    })
    @PostMapping("/email/resend-verification")
    public ResponseEntity<Void> resendConfirmationEmail(
            @RequestBody @Valid ResendVerificationRequest request) {
        authenticationService.resendVerificationEmail(request);
        return ResponseEntity.accepted().build();
    }

    /**
     * Initiates password recovery process.
     *
     * @param request {@link UserUpdateRequest} - user email
     * @return {@link ResponseEntity} with HTTP status 202 (Accepted)
     * @throws jakarta.validation.ConstraintViolationException if email is invalid
     */
    @Operation(summary = "Password reset request", description = "Sends email with password reset link")
    @ApiResponse(responseCode = "202", description = "Password reset email sent")
    @PostMapping("/password/reset-request")
    public ResponseEntity<Void> forgotPassword(@RequestBody @Valid UserUpdateRequest request) {
        authenticationService.forgotPassword(request);
        return ResponseEntity.accepted().build();
    }


    /**
     * Resets user password using recovery token.
     *
     * @param request {@link ResetPasswordRequest} - new password and token
     * @return {@link ResponseEntity} with HTTP status 200 (OK)
     * @throws jakarta.validation.ConstraintViolationException if request data is invalid
     * @throws TokenExpiredException                           if token is expired
     */
    @Operation(summary = "Password reset", description = "Resets password using recovery token")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Password successfully reset"),
            @ApiResponse(responseCode = "400", description = "Invalid or expired token", content = @Content)
    })
    @PatchMapping("/password")
    public ResponseEntity<Void> resetPassword(@RequestBody @Valid ResetPasswordRequest request) {
        authenticationService.resetPassword(request);
        return ResponseEntity.ok().build();
    }

    /**
     * Refreshes access token using refresh token.
     *
     * @param refreshToken refresh token from cookie
     * @param response     {@link HttpServletResponse} to set new refresh token
     * @return {@link ResponseEntity} with new {@link AuthenticationResponse} (access token)
     * @throws jakarta.validation.ConstraintViolationException if refresh token is invalid
     * @throws TokenExpiredException                           if refresh token is expired
     */
    @Operation(summary = "Refresh access token", description = "Refreshes access token using refresh token from cookie and returns new access token")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Token successfully refreshed",
                    content = @Content(schema = @Schema(implementation = AuthenticationResponse.class))),
            @ApiResponse(responseCode = "401", description = "Invalid or expired refresh token", content = @Content)
    })
    @PostMapping("/token/refresh")
    public ResponseEntity<AuthenticationResponse> refreshToken(
            @CookieValue(name = "refresh_token") @NotBlank String refreshToken,
            HttpServletResponse response) {
        var tokens = authenticationService.refreshToken(refreshToken);
        var cookie = CookieUtils.createTokenCookie(tokens.getRefreshToken(), Duration.ofMillis(durationDays));
        response.addHeader(HttpHeaders.SET_COOKIE, cookie.toString());
        return ResponseEntity.ok(new AuthenticationResponse(tokens.getToken()));
    }

    /**
     * Logs out user from current session.
     *
     * @param refreshToken refresh token from cookie
     * @param response     {@link HttpServletResponse} to clear cookie
     * @return {@link ResponseEntity} with HTTP status 204 (No Content)
     * @throws jakarta.validation.ConstraintViolationException if refresh token is invalid
     */
    @Operation(summary = "Logout from current session", description = "Revokes current refresh token and clears cookie")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "204", description = "Logout successful"),
            @ApiResponse(responseCode = "400", description = "Invalid refresh token", content = @Content)
    })
    @DeleteMapping("/sessions/current")
    public ResponseEntity<Void> logout(
            @CookieValue(name = "refresh_token") @NotBlank String refreshToken,
            HttpServletResponse response) {
        refreshTokenService.revokeRefreshToken(refreshToken);
        var cookie = CookieUtils.createTokenCookie(StringUtils.EMPTY, Duration.ZERO);
        response.addHeader(HttpHeaders.SET_COOKIE, cookie.toString());
        return ResponseEntity.noContent().build();
    }

    /**
     * Logs out user from all active sessions.
     *
     * @param refreshToken refresh token from cookie
     * @param response     {@link HttpServletResponse} to clear cookie
     * @return {@link ResponseEntity} with HTTP status 204 (No Content)
     * @throws jakarta.validation.ConstraintViolationException if refresh token is invalid
     */
    @Operation(summary = "Logout from all devices", description = "Revokes all user refresh tokens and clears cookie")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "204", description = "All sessions terminated"),
            @ApiResponse(responseCode = "400", description = "Invalid refresh token", content = @Content)
    })
    @DeleteMapping("/sessions")
    public ResponseEntity<Void> revokeAllRefreshTokens(
            @CookieValue(name = "refresh_token") @NotBlank String refreshToken,
            HttpServletResponse response) {
        refreshTokenService.revokeAllUserTokens(refreshToken);
        var cookie = CookieUtils.createTokenCookie(StringUtils.EMPTY, Duration.ZERO);
        response.addHeader(HttpHeaders.SET_COOKIE, cookie.toString());
        return ResponseEntity.noContent().build();
    }
}
