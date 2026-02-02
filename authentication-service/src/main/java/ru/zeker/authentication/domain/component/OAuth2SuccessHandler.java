package ru.zeker.authentication.domain.component;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpHeaders;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.stereotype.Component;
import ru.zeker.authentication.exception.OAuth2ProviderException;
import ru.zeker.authentication.service.JwtService;
import ru.zeker.authentication.service.OAuth2Service;
import ru.zeker.authentication.util.CookieUtils;

import java.io.IOException;
import java.time.Duration;
import java.util.Map;
import java.util.Objects;

@Component
@RequiredArgsConstructor
@Slf4j
public class OAuth2SuccessHandler implements AuthenticationSuccessHandler {
    private final JwtService jwtService;
    private final OAuth2Service oAuth2Service;
    private final ObjectMapper objectMapper;

    /**
     * Handler for successful OAuth2 authentication.
     * <p>
     * If user is not found, registers them.
     * If user is found but doesn't have OAuth2 authentication in database, adds it.
     * If user is found, generates access and refresh tokens.
     * If authentication fails, returns error data.
     * </p>
     *
     * @param request        HTTP request
     * @param response       HTTP response
     * @param authentication authentication result
     * @throws IOException input/output error
     */
    @Override
    public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response, Authentication authentication) throws IOException {
        log.info("OAuth2 successful authentication handler triggered: remote={}, uri={}",
                request.getRemoteAddr(), request.getRequestURI());
        try {
            var oAuth2User = (OAuth2User) authentication.getPrincipal();
            log.debug("OAuth2User principal obtained: authorities={}", authentication.getAuthorities());

            var provider = getOAuth2Provider(authentication);
            if (Objects.isNull(provider)) {
                log.error("Failed to get OAuth2Provider");
                throw new OAuth2ProviderException("Failed to get OAuth2Provider");
            }
            log.info("OAuth2Provider obtained: {}", provider);

            var user = oAuth2Service.processOauth2User(oAuth2User, provider);

            log.debug("User resolved: id={}, email={}, enabled={}", user.getId(), user.getEmail(), user.isEnabled());

            var accessToken = jwtService.generateAccessToken(user);
            var refreshToken = jwtService.generateRefreshToken(user);

            var refreshCookie = CookieUtils.createTokenCookie(refreshToken, Duration.ofDays(7));
            response.addHeader(HttpHeaders.SET_COOKIE, refreshCookie.toString());

            response.setContentType("application/json;charset=UTF-8");
            objectMapper.writeValue(response.getWriter(), Map.of(
                    "access_token", accessToken,
                    "token_type", "Bearer"
            ));
        } catch (Exception e) {
            log.error("OAuth2SuccessHandler error: {}", e.getMessage(), e);
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            response.setContentType("application/json;charset=UTF-8");
            objectMapper.writeValue(response.getWriter(), Map.of(
                    "error", "OAuth2 authentication failed",
                    "message", e.getMessage()
            ));
        }
    }

    /**
     * Extracts the OAuth2 provider name from the specified authentication token.
     *
     * @param authentication authentication token from which to extract the provider
     * @return OAuth2 provider name if available, otherwise null
     */
    private String getOAuth2Provider(Authentication authentication) {
        if (authentication instanceof OAuth2AuthenticationToken oauthToken) {
            return oauthToken.getAuthorizedClientRegistrationId();
        }
        return null;
    }
}
