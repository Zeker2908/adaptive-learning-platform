package ru.zeker.common.util;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.JwtParser;
import io.jsonwebtoken.Jwts;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import ru.zeker.common.config.JwtProperties;
import ru.zeker.common.consts.JwtKeys;

import java.io.IOException;
import java.security.KeyFactory;
import java.security.NoSuchAlgorithmException;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.X509EncodedKeySpec;
import java.util.Base64;
import java.util.Date;
import java.util.Objects;
import java.util.Optional;
import java.util.function.Function;

@RequiredArgsConstructor
public class JwtUtils {
    private final JwtProperties jwtProperties;
    private JwtParser jwtParser;

    @PostConstruct
    public void init() {
        try {
            if (Objects.isNull(jwtProperties.getPublicKeyPath()) || !jwtProperties.getPublicKeyPath().exists()) {
                throw new IllegalStateException("The public key is not set");
            }

            var publicKeyContent = new String(jwtProperties.getPublicKeyPath().getInputStream().readAllBytes());

            var publicKeyPEM = publicKeyContent
                    .replace("-----BEGIN PUBLIC KEY-----", "")
                    .replace("-----END PUBLIC KEY-----", "")
                    .replaceAll("\\s+", "");

            var keyBytes = Base64.getDecoder().decode(publicKeyPEM);
            var spec = new X509EncodedKeySpec(keyBytes);
            var kf = KeyFactory.getInstance("EC");
            var publicKey = kf.generatePublic(spec);

            this.jwtParser = Jwts.parser()
                    .verifyWith(publicKey)
                    .build();

        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException("EC algorithm is not supported", e);
        } catch (InvalidKeySpecException e) {
            throw new IllegalStateException("Incorrect key format", e);
        } catch (IllegalArgumentException | IOException e) {
            throw new IllegalStateException("Base64 decoding error", e);
        } catch (Exception e) {
            throw new IllegalStateException("JWT initialization error", e);
        }
    }

    public Claims extractAllClaims(String token) {
        return parseClaimsJws(token);
    }

    protected Claims parseClaimsJws(String token) {
        try {
            return jwtParser.parseSignedClaims(token).getPayload();
        } catch (ExpiredJwtException e) {
            throw e;
        } catch (Exception e) {
            throw new RuntimeException("Failed to parse token: " + e.getMessage(), e);
        }
    }

    public <T> T extractClaim(String token, Function<Claims, T> claimsResolver) {
        final var claims = extractAllClaims(token);
        return claimsResolver.apply(claims);
    }

    public String extractUsername(String token) {
        return extractClaim(token, Claims::getSubject);
    }

    public String getUsername(Claims claims) {
        return Optional.ofNullable(claims.getSubject())
                .orElseThrow(() -> new IllegalArgumentException("JWT token does not contain subject"));
    }

    public Date extractExpiration(String token) {
        return extractClaim(token, Claims::getExpiration);
    }

    public Date getExpiration(Claims claims) {
        return Optional.ofNullable(claims.getExpiration())
                .orElseThrow(() -> new IllegalArgumentException("JWT token does not contain expiration"));
    }

    public Date extractIssuedAt(String token) {
        return extractClaim(token, Claims::getIssuedAt);
    }

    public boolean isTokenExpired(String token) {
        return extractExpiration(token).before(new Date());
    }

    public boolean isTokenExpired(Claims claims) {
        return getExpiration(claims).before(new Date());
    }

    public boolean isValidUsername(String token, String username) {
        return extractUsername(token).equals(username);
    }

    public boolean isValidUsername(Claims claims, String username) {
        return getUsername(claims).equals(username);
    }

    public String extractUserId(String token) {
        return Optional.ofNullable(extractClaim(token, claims -> claims.get(JwtKeys.ID_KEY, String.class)))
                .orElseThrow(() -> new IllegalArgumentException("JWT token does not contain user ID"));
    }

    public String getUserId(Claims claims) {
        return Optional.ofNullable(claims.get(JwtKeys.ID_KEY, String.class))
                .orElseThrow(() -> new IllegalArgumentException("JWT token does not contain user ID"));
    }

    public Long extractVersion(String token) {
        return Optional.ofNullable(extractClaim(token, claims -> claims.get(JwtKeys.VERSION_KEY, Long.class)))
                .orElseThrow(() -> new IllegalArgumentException("JWT token does not contain version"));
    }

    public Long getVersion(Claims claims) {
        return Optional.ofNullable(claims.get(JwtKeys.VERSION_KEY, Long.class))
                .orElseThrow(() -> new IllegalArgumentException("JWT token does not contain version"));
    }

    public boolean isValidVersion(Claims claims, Long version) {
        return getVersion(claims).equals(version);
    }

    public String extractRole(String token) {
        return Optional.ofNullable(extractClaim(token, claims -> claims.get(JwtKeys.ROLE_KEY, String.class)))
                .orElseThrow(() -> new IllegalArgumentException("JWT token does not contain role"));
    }

    public String getRole(Claims claims) {
        return Optional.ofNullable(claims.get(JwtKeys.ROLE_KEY, String.class))
                .orElseThrow(() -> new IllegalArgumentException("JWT token does not contain role"));
    }

}
