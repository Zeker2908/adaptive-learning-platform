package ru.zeker.authentication.service;

import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;
import ru.zeker.authentication.domain.model.entity.User;
import ru.zeker.common.config.JwtProperties;
import ru.zeker.common.consts.JwtKeys;

import java.io.IOException;
import java.security.Key;
import java.security.KeyFactory;
import java.security.NoSuchAlgorithmException;
import java.security.interfaces.ECPrivateKey;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.PKCS8EncodedKeySpec;
import java.util.Base64;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

@Service
@RequiredArgsConstructor
public class JwtService {

    private final JwtProperties jwtProperties;
    private Key privateKey;

    @PostConstruct
    public void init() {
        try {
            if (Objects.isNull(jwtProperties.getPrivateKeyPath()) || !jwtProperties.getPrivateKeyPath().exists()) {
                throw new IllegalStateException("The private key is not set.");
            }

            var privateKeyContent = new String(jwtProperties.getPrivateKeyPath().getInputStream().readAllBytes());

            var privateKeyPEM = privateKeyContent
                    .replace("-----BEGIN PRIVATE KEY-----", "")
                    .replace("-----END PRIVATE KEY-----", "")
                    .replaceAll("\\s", "");

            var keyBytes = Base64.getDecoder().decode(privateKeyPEM);
            var spec = new PKCS8EncodedKeySpec(keyBytes);
            var kf = KeyFactory.getInstance("EC");
            this.privateKey = kf.generatePrivate(spec);
            if (!(this.privateKey instanceof ECPrivateKey)) {
                throw new IllegalStateException("The key is not an EC private key.");
            }
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException("EC algorithm is not supported", e);
        } catch (InvalidKeySpecException e) {
            throw new IllegalStateException("Invalid key format", e);
        } catch (IllegalArgumentException | IOException e) {
            throw new IllegalStateException("Base64 decoding error", e);
        } catch (Exception e) {
            throw new IllegalStateException("JWT initialization error", e);
        }
    }

    public String generateAccessToken(UserDetails userDetails) {
        var claims = new HashMap<String, Object>();
        if (userDetails instanceof User customUserDetails) {
            claims.put(JwtKeys.ID_KEY, customUserDetails.getId());
            claims.put(JwtKeys.ROLE_KEY, customUserDetails.getRole());
        }
        return generateToken(userDetails, claims, jwtProperties.getAccess().getExpiration());
    }

    public String generateRefreshToken(UserDetails userDetails) {
        var claims = new HashMap<String, Object>();
        if (userDetails instanceof User customUserDetails) {
            claims.put(JwtKeys.ID_KEY, customUserDetails.getId());
            claims.put(JwtKeys.ROLE_KEY, customUserDetails.getRole());
        }
        return generateToken(userDetails, claims, jwtProperties.getRefresh().getExpiration());
    }

    public String generateEmailToken(UserDetails userDetails) {
        var claims = new HashMap<String, Object>();
        if (userDetails instanceof User customUserDetails) {
            claims.put(JwtKeys.ID_KEY, customUserDetails.getId());
            claims.put(JwtKeys.VERSION_KEY, customUserDetails.getVersion());
        }
        return generateToken(userDetails, claims, jwtProperties.getAccess().getExpiration());
    }

    private String generateToken(UserDetails userDetails, Map<String, Object> claims, long expiration) {
        var currentTimeMillis = System.currentTimeMillis();

        return Jwts.builder()
                .setClaims(claims)
                .setSubject(userDetails.getUsername())
                .setIssuedAt(new Date(currentTimeMillis))
                .setExpiration(new Date(currentTimeMillis + expiration))
                .signWith(privateKey, SignatureAlgorithm.ES256)
                .compact();
    }

}
