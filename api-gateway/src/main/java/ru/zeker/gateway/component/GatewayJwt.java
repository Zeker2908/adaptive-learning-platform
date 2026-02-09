package ru.zeker.gateway.component;

import com.github.benmanes.caffeine.cache.Cache;
import io.jsonwebtoken.Claims;
import jakarta.annotation.PostConstruct;
import org.springframework.stereotype.Component;
import ru.zeker.common.config.JwtProperties;
import ru.zeker.common.util.JwtUtils;

@Component
public class GatewayJwt extends JwtUtils {
    private final Cache<String, Claims> claimsCache;

    public GatewayJwt(JwtProperties jwtProperties, Cache<String, Claims> claimsCache) {
        super(jwtProperties);
        this.claimsCache = claimsCache;
    }
    @PostConstruct
    public void init() {
        super.init();
    }

    @Override
    public Claims extractAllClaims(String token) {
        return claimsCache.get(token, this::parseClaimsJws);
    }

    public void invalidateToken(String token) {
        claimsCache.invalidate(token);
    }

    public void invalidateAll() {
        claimsCache.invalidateAll();
    }

}
