package ru.zeker.gateway.config;

import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import com.github.benmanes.caffeine.cache.RemovalCause;
import io.jsonwebtoken.Claims;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.codec.json.Jackson2JsonEncoder;
import ru.zeker.common.config.JwtProperties;
import ru.zeker.common.util.JwtUtils;

import java.util.concurrent.TimeUnit;

@Configuration
@RequiredArgsConstructor
@Slf4j
public class GatewayConfig {

    @Bean
    public JwtProperties jwtProperties() {
        return new JwtProperties();
    }

    @Bean
    public Cache<String, Claims> claimsCache(JwtProperties jwtProperties) {
        long cacheTtlMs = Math.max(0, jwtProperties.getAccess().getExpiration() - TimeUnit.MINUTES.toMillis(1));

        return Caffeine.newBuilder()
                .maximumSize(100_000)
                .expireAfterWrite(cacheTtlMs, TimeUnit.MILLISECONDS)
                .evictionListener((String key, Claims value, RemovalCause cause) ->
                        log.debug("Токен выселен из кэша: {}, причина: {}", key, cause))
                .removalListener((String key, Claims value, RemovalCause cause) ->
                        log.debug("Токен удален из кэша: {}, причина: {}", key, cause))
                .recordStats()
                .build();
    }

    @Bean
    public JwtUtils jwtUtils(JwtProperties jwtProperties, Cache<String, Claims> claimsCache) {
        return new JwtUtils(jwtProperties, claimsCache);
    }

    @Bean
    public Jackson2JsonEncoder jsonEncoder() {
        return new Jackson2JsonEncoder();
    }
}