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
        return Caffeine.newBuilder()
                .maximumSize(100_000)
                .expireAfterWrite(jwtProperties.getAccess().getExpiration(), TimeUnit.MILLISECONDS)
                .evictionListener((String key, Claims value, RemovalCause cause) ->
                        log.debug("The token has been evicted from the cache: {}, cause: {}", key, cause))
                .removalListener((String key, Claims value, RemovalCause cause) ->
                        log.debug("The token has been evicted from the cache: {}, cause: {}", key, cause))
                .recordStats()
                .build();
    }

    @Bean
    public Jackson2JsonEncoder jsonEncoder() {
        return new Jackson2JsonEncoder();
    }
}