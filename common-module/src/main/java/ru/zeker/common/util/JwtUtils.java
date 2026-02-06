package ru.zeker.common.util;

import com.github.benmanes.caffeine.cache.Cache;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.JwtParser;
import io.jsonwebtoken.Jwts;
import jakarta.annotation.PostConstruct;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import ru.zeker.common.config.JwtProperties;
import ru.zeker.common.exception.AuthException;
import ru.zeker.common.exception.ErrorCode;

import java.io.IOException;
import java.security.Key;
import java.security.KeyFactory;
import java.security.NoSuchAlgorithmException;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.X509EncodedKeySpec;
import java.util.Base64;
import java.util.Date;
import java.util.Objects;
import java.util.function.Function;

@RequiredArgsConstructor
@Getter
public class JwtUtils {
    private final JwtProperties jwtProperties;
    private final Cache<String, Claims> claimsCache;

    private Key publicKey;
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
            this.publicKey = kf.generatePublic(spec);

            this.jwtParser = Jwts.parserBuilder()
                    .setSigningKey(this.publicKey)
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
        return claimsCache.get(token, this::parseClaimsJws);
    }

    private Claims parseClaimsJws(String token) {
        try {
            return jwtParser.parseClaimsJws(token).getBody();
        } catch (ExpiredJwtException e) {
            throw new AuthException("The token has expired", ErrorCode.TOKEN_EXPIRED);
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

    public Date extractExpiration(String token) {
        return extractClaim(token, Claims::getExpiration);
    }

    public Date extractIssuedAt(String token) {
        return extractClaim(token, Claims::getIssuedAt);
    }

    public boolean isTokenExpired(String token) {
        return extractExpiration(token).before(new Date());
    }

    public boolean isValidUsername(String token, String username) {
        return extractUsername(token).equals(username);
    }

    public void invalidateToken(String token) {
        claimsCache.invalidate(token);
    }

    public void invalidateAll() {
        claimsCache.invalidateAll();
    }
}
