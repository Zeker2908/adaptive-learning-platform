package ru.zeker.gateway.component;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.cloud.gateway.route.Route;
import org.springframework.cloud.gateway.support.ServerWebExchangeUtils;
import org.springframework.core.Ordered;
import org.springframework.core.ResolvableType;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.codec.json.Jackson2JsonEncoder;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;
import ru.zeker.common.util.JwtUtils;
import ru.zeker.gateway.exception.AuthException;

import java.time.Instant;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;

import static ru.zeker.common.headers.AppHeaders.USER_ID;
import static ru.zeker.common.headers.AppHeaders.USER_NAME;
import static ru.zeker.common.headers.AppHeaders.USER_ROLE;

@Slf4j
@Component
@RequiredArgsConstructor
public class JwtValidationFilter implements GlobalFilter, Ordered {
    private static final String BEARER_PREFIX = "Bearer ";
    private static final String AUTH_REQUIRED_KEY = "auth-required";
    private static final String REQUIRED_ROLE_KEY = "required-role";
    private static final String TOKEN_EXPIRED_REASON = "TOKEN_EXPIRED";

    private final JwtUtils jwtUtils;
    private final Jackson2JsonEncoder jsonEncoder;

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        return isAuthRequired(exchange)
                .flatMap(required -> {
                    if (!required) {
                        return chain.filter(exchange);
                    }
                    return extractClaims(exchange)
                            .flatMap(claims -> verifyRole(exchange, claims))
                            .flatMap(claims -> chain.filter(withUserHeaders(exchange, claims)));
                })
                .onErrorResume(AuthException.class, ex -> writeError(exchange, ex));
    }

    private Mono<Boolean> isAuthRequired(ServerWebExchange exchange) {
        Route route = exchange.getAttribute(ServerWebExchangeUtils.GATEWAY_ROUTE_ATTR);
        boolean required = Optional.ofNullable(route)
                .map(Route::getMetadata)
                .map(meta -> Boolean.parseBoolean(meta.getOrDefault(AUTH_REQUIRED_KEY, "true").toString()))
                .orElse(true);
        return Mono.just(required);
    }

    private Mono<Claims> extractClaims(ServerWebExchange exchange) {
        String authHeader = exchange.getRequest().getHeaders().getFirst(HttpHeaders.AUTHORIZATION);
        if (StringUtils.isBlank(authHeader) || !authHeader.startsWith(BEARER_PREFIX)) {
            return Mono.error(new AuthException("Authorization header missing", HttpStatus.UNAUTHORIZED));
        }

        String token = authHeader.substring(BEARER_PREFIX.length());

        return Mono.fromCallable(() -> {
                    try {
                        if (jwtUtils.isTokenExpired(token)) {
                            throw new AuthException("The token has expired.", HttpStatus.UNAUTHORIZED, TOKEN_EXPIRED_REASON);
                        }
                        return jwtUtils.extractAllClaims(token);
                    } catch (AuthException e) {
                        log.warn(e.getMessage());
                        throw e;
                    } catch (JwtException e) {
                        log.warn("Invalid JWT: {}", e.getMessage());
                        throw new AuthException("Invalid token", HttpStatus.UNAUTHORIZED);
                    } catch (Exception e) {
                        log.warn("Failed to parse token {}", e.getMessage());
                        throw new AuthException("Invalid token", HttpStatus.UNAUTHORIZED);
                    }
                })
                .subscribeOn(Schedulers.boundedElastic());
    }

    private Mono<Claims> verifyRole(ServerWebExchange exchange, Claims claims) {
        String userRole = claims.get("role", String.class);
        if (Objects.isNull(userRole)) {
            log.warn("The user's role is not specified in the token.");
            return Mono.error(new AuthException("The user's role is not specified in the token.", HttpStatus.FORBIDDEN));
        }
        Route route = exchange.getAttribute(ServerWebExchangeUtils.GATEWAY_ROUTE_ATTR);
        String requiredRole = Optional.ofNullable(route)
                .map(Route::getMetadata)
                .map(meta -> meta.get(REQUIRED_ROLE_KEY))
                .map(Object::toString)
                .orElse(null);
        if (Objects.nonNull(requiredRole) && !requiredRole.equals(userRole)) {
            log.warn("Insufficient privileges");
            return Mono.error(new AuthException("Insufficient privileges", HttpStatus.FORBIDDEN));
        }
        return Mono.just(claims);
    }

    private ServerWebExchange withUserHeaders(ServerWebExchange exchange, Claims claims) {
        ServerHttpRequest mutated = exchange.getRequest().mutate()
                .header(USER_ID, claims.get("id", String.class))
                .header(USER_NAME, claims.getSubject())
                .header(USER_ROLE, claims.get("role", String.class))
                .headers(headers -> headers.remove(HttpHeaders.AUTHORIZATION))
                .build();
        return exchange.mutate().request(mutated).build();
    }


    private Mono<Void> writeError(ServerWebExchange exchange, AuthException ex) {
        ServerHttpResponse response = exchange.getResponse();
        response.setStatusCode(ex.getStatus());
        response.getHeaders().setContentType(MediaType.APPLICATION_JSON);

        Map<String, Object> body = new LinkedHashMap<>();
        body.put("timestamp", Instant.now().toString());
        body.put("path", exchange.getRequest().getPath().toString());
        body.put("status", ex.getStatus().value());
        body.put("error", ex.getStatus().getReasonPhrase());
        body.put("message", ex.getMessage());
        if (StringUtils.isNotBlank(ex.getReason())) {
            body.put("reason", ex.getReason());
        }

        return response.writeWith(
                jsonEncoder.encode(
                        Mono.just(body),
                        response.bufferFactory(),
                        ResolvableType.forClassWithGenerics(Map.class, String.class, Object.class),
                        MediaType.APPLICATION_JSON,
                        null
                )
        );
    }


    @Override
    public int getOrder() {
        return Ordered.HIGHEST_PRECEDENCE;
    }
}
