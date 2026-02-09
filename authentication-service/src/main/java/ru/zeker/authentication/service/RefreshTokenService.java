package ru.zeker.authentication.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import ru.zeker.authentication.domain.model.entity.RefreshToken;
import ru.zeker.authentication.domain.model.entity.User;
import ru.zeker.authentication.exception.TokenExpiredException;
import ru.zeker.authentication.exception.TokenNotFoundException;
import ru.zeker.authentication.exception.UserNotFoundException;
import ru.zeker.authentication.repository.RefreshTokenRepository;
import ru.zeker.common.config.JwtProperties;
import ru.zeker.common.exception.ErrorCode;
import ru.zeker.common.util.JwtUtils;

import java.util.Date;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

/**
 * Implementation of service for managing refresh tokens
 * Provides creation, verification, rotation, and revocation of refresh tokens
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class RefreshTokenService {
    private final JwtService jwtService;
    private final JwtUtils jwtUtils;
    private final JwtProperties jwtProperties;
    private final RefreshTokenRepository refreshTokenRepository;

    /**
     * Creates a new refresh token for a user
     *
     * @param user user for whom the token is created
     * @return refresh token string
     */
    public String createRefreshToken(User user) {
        log.debug("Creating new refresh token for user with ID: {}", user.getId());

        var token = jwtService.generateRefreshToken(user);
        var ttlMillis = jwtProperties.getRefresh().getExpiration() - 1000;

        var refreshToken = RefreshToken.builder()
                .token(token)
                .userId(user.getId())
                .ttl(ttlMillis)
                .build();

        var savedToken = refreshTokenRepository.save(refreshToken);

        String formattedDuration = formatDuration(ttlMillis);
        Date expirationDate = new Date(System.currentTimeMillis() + ttlMillis);

        log.debug("Refresh token saved | TTL: {} | Expires at: {}",
                formattedDuration,
                expirationDate);

        return savedToken.getToken();
    }

    /**
     * Verifies the validity of a refresh token
     *
     * @param token refresh token string to verify
     * @return RefreshToken object if token is valid
     * @throws TokenExpiredException  if token is expired or revoked
     * @throws TokenNotFoundException if token is not found
     */
    public RefreshToken verifyRefreshToken(String token) {
        log.debug("Verifying refresh token");

        return refreshTokenRepository.findByToken(token)
                .map(t -> {
                    log.debug("Refresh token valid for user with ID: {}", t.getUserId());
                    return t;
                })
                .orElseThrow(() -> {
                    log.warn("Token not found in database");
                    return new TokenNotFoundException("Refresh token not found");
                });
    }

    /**
     * Rotates refresh token by deleting the old one and creating a new one
     *
     * @param token old refresh token object
     * @return new refresh token string
     */
    public String rotateRefreshToken(RefreshToken token, User user) {
        log.debug("Rotating refresh token for user with ID: {}", token.getUserId());

        refreshTokenRepository.delete(token);
        log.debug("Old refresh token deleted");

        var newToken = createRefreshToken(user);

        log.info("Refresh token successfully rotated for user with ID: {}", token.getUserId());
        return newToken;
    }

    /**
     * Revokes a refresh token, making it invalid
     *
     * @param token refresh token string to revoke
     */
    public void revokeRefreshToken(String token) {
        log.debug("Request to revoke refresh token");

        refreshTokenRepository.findByToken(token)
                .ifPresent(t -> {
                    log.info("Revoking refresh token for user with ID: {}", t.getUserId());
                    refreshTokenRepository.delete(t);
                });
    }

    /**
     * Revokes all refresh tokens for a user
     *
     * @param token user token
     */
    public void revokeAllUserTokens(String token) {
        revokeAllUserTokens(UUID.fromString(jwtUtils.extractUserId(token)));
    }

    public void revokeAllUserTokens(UUID userId) {
        log.info("Revoking all refresh tokens for user with ID: {}", userId);

        var tokens = refreshTokenRepository.findAllByUserId(userId);

        if (tokens.isEmpty()) {
            log.warn("User with ID: {} has no refresh tokens", userId);
            throw new UserNotFoundException("User with ID: " + userId + " has no refresh tokens", ErrorCode.USER_NOT_HAVE_ACTIVE_SESSIONS);
        }

        refreshTokenRepository.deleteAll(tokens);
    }

    private String formatDuration(long millis) {
        long hours = TimeUnit.MILLISECONDS.toHours(millis);
        long minutes = TimeUnit.MILLISECONDS.toMinutes(millis) % 60;
        long seconds = TimeUnit.MILLISECONDS.toSeconds(millis) % 60;

        return String.format("%dh %dm %ds", hours, minutes, seconds);
    }
}
