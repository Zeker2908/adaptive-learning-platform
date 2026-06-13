package ru.zeker.authentication.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.core.annotation.Order;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserRequest;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserService;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.security.web.SecurityFilterChain;
import ru.zeker.authentication.domain.component.OAuth2SuccessHandler;
import ru.zeker.authentication.config.DemoLoginProperties;
import ru.zeker.common.config.JwtProperties;

import java.util.Map;

@Configuration
@Import(AuthenticationBeansConfig.class)
@EnableWebSecurity
@RequiredArgsConstructor
@EnableConfigurationProperties({JwtProperties.class, DemoLoginProperties.class})
public class SecurityConfig {
    private final OAuth2SuccessHandler oAuth2SuccessHandler;
    private final AuthenticationProvider authenticationProvider;
    private final OAuth2UserService<OAuth2UserRequest, OAuth2User> oAuth2UserService;
    private final ObjectMapper objectMapper;

    /**
     * Configures the {@link SecurityFilterChain} for OAuth2 endpoints.
     *
     * <p>This method configures the {@link SecurityFilterChain} for OAuth2 endpoints and is used to handle OAuth2 authentication requests.
     * The chain is configured to allow all requests to OAuth2 endpoints, disable CSRF protection,
     * and use a custom {@link OAuth2UserService} to handle the user information endpoint. The chain also configures a
     * custom success handler and a failure handler.
     *
     * <p>The chain is configured to use the {@link OAuth2SuccessHandler} to handle successful OAuth2 authentication requests.
     * The handler is configured to redirect the user to the default success URL after successful
     * authentication. *
     * <p>The chain is also configured to use a custom failure handler to handle OAuth2 authentication failures. The handler
     * is configured to redirect the user to the default failure URL after authentication failure.
     *
     * <p>The chain is configured to create a session for OAuth2 authentication requests, as required by the OAuth2 specification.
     *
     * @param http : The {@link HttpSecurity} object used to configure the filter chain
     * @return the configured {@link SecurityFilterChain}
     * @throws Exception if an error occurs while configuring the filter chain
     */
    @Bean
    @Order(1)
    public SecurityFilterChain oauthEndpointsFilterChain(HttpSecurity http) throws Exception {
        http
                .securityMatcher("/oauth2/**", "/login/oauth2/**")
                .cors(AbstractHttpConfigurer::disable)
                .csrf(AbstractHttpConfigurer::disable)
                .authorizeHttpRequests(auth -> auth.anyRequest().permitAll())
                .oauth2Login(oauth -> oauth
                        .authorizationEndpoint(e -> e.baseUri("/oauth2/authorization"))
                        .redirectionEndpoint(e -> e.baseUri("/login/oauth2/code/*"))
                        .userInfoEndpoint(u -> u.userService(oAuth2UserService))
                        .successHandler(oAuth2SuccessHandler)
                        .failureHandler((request, response, exception) -> {
                            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
                            response.setContentType("application/json;charset=UTF-8");
                            objectMapper.writeValue(response.getWriter(), Map.of(
                                    "error", "OAuth2 authentication failed",
                                    "message", exception.getMessage()
                            ));
                        })
                )

                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.IF_REQUIRED));
        return http.build();
    }

    /**
     * Generates a security filter chain for authentication endpoints.
     *
     * <p>Authentication endpoints do not require authentication, so CSRF protection is disabled for them
     * and authorization is allowed for all requests.
     * </p>
     *
     * @param http : {@link HttpSecurity} object used to configure the filter chain
     * @return the configured {@link SecurityFilterChain}
     * @throws Exception if an error occurs while configuring the filter chain
     */
    @Bean
    @Order(2)
    public SecurityFilterChain authEndpointsFilterChain(HttpSecurity http) throws Exception {
        http
                .securityMatcher("/auth/**")
                .cors(AbstractHttpConfigurer::disable)
                .csrf(AbstractHttpConfigurer::disable)
                .authorizeHttpRequests(auth -> auth.anyRequest().permitAll())
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .authenticationProvider(authenticationProvider);
        return http.build();
    }

    /**
     * Configures the primary security filter chain for protected endpoints.
     *
     * <p>This filter chain applies to all requests that are not handled
     * by the OAuth2 and authentication filter chains. It requires all requests to be
     * authenticated, disables CSRF protection, and configures session management
     * as STATELESS, which aligns with the REST API approach.</p>
     *
     * <p>The chain also adds a custom header validation filter before
     * the default username/password authentication filter.</p>
     *
     * @param http {@link HttpSecurity} object used to configure the filter chain
     * @return the configured {@link SecurityFilterChain}
     * @throws Exception if an error occurs while configuring the filter chain
     */
    @Bean
    @Order(3)
    public SecurityFilterChain mainFilterChain(HttpSecurity http) throws Exception {
        http
                .authorizeHttpRequests(auth -> auth
                        .anyRequest().permitAll())
                .cors(AbstractHttpConfigurer::disable)
                .csrf(AbstractHttpConfigurer::disable)
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS));
        return http.build();
    }

}
