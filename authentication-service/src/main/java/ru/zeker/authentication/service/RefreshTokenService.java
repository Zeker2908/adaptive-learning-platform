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

import java.util.Date;
import java.util.Set;
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
    private final JwtProperties jwtProperties;
    private final RefreshTokenRepository refreshTokenRepository;
    private final UserService userService;

    /**
     * Creates a new refresh token for a user
     *
     * @param user user for whom the token is created
     * @return refresh token string
     */
    public String createRefreshToken(User user) {
        log.debug("Creating new refresh token for user with ID: {}", user.getId());

        String token = jwtService.generateRefreshToken(user);
        Date expiryDate = new Date(System.currentTimeMillis() + jwtProperties.getRefresh().getExpiration());
        long ttlSeconds = TimeUnit.MILLISECONDS.toSeconds(expiryDate.getTime() - System.currentTimeMillis());

        RefreshToken refreshToken = RefreshToken.builder()
                .token(token)
                .userId(user.getId())
                .expiryDate(expiryDate)
                .ttl(ttlSeconds)
                .build();

        RefreshToken savedToken = refreshTokenRepository.save(refreshToken);
        log.debug("Refresh token successfully saved to database, expiration: {} seconds", ttlSeconds);

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
                    if (t.getExpiryDate().before(new Date())) {
                        log.warn("Attempt to use expired token for user with ID: {}", t.getUserId());
                        refreshTokenRepository.delete(t);
                        throw new TokenExpiredException("Token expiration date has passed");
                    }

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
    public String rotateRefreshToken(RefreshToken token) {
        log.debug("Rotating refresh token for user with ID: {}", token.getUserId());

        refreshTokenRepository.delete(token);
        log.debug("Old refresh token deleted");

        User user = userService.findById(token.getUserId());
        String newToken = createRefreshToken(user);

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
        UUID userId = jwtService.extractUserId(token);
        log.info("Revoking all refresh tokens for user with ID: {}", userId);

        Set<RefreshToken> tokens = refreshTokenRepository.findAllByUserId(userId).orElseThrow(() -> {
            log.warn("User with ID: {} has no refresh tokens", userId);
            return new UserNotFoundException("User with ID: " + userId + " has no refresh tokens");
        });

        refreshTokenRepository.deleteAll(tokens);

        log.info("Revoked {} tokens for user with ID: {}", tokens.size(), userId);
    }
}
