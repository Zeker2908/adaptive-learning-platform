package ru.zeker.authentication.util;

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseCookie;
import org.springframework.stereotype.Component;

import java.time.Duration;

@Component
public class CookieService {

    @Value("${app.cookie.domain:}")
    private String domain;

    @Value("${app.cookie.same-site:Lax}")
    private String sameSite;

    @Value("${app.cookie.secure:true}")
    private boolean secure;

    public ResponseCookie createTokenCookie(String value, Duration duration) {
        var builder = ResponseCookie.from("refresh_token", value)
                .httpOnly(true)
                .secure(secure)
                .path("/")
                .maxAge(duration)
                .sameSite(sameSite);

        if (StringUtils.isNotBlank(domain)) {
            builder.domain(domain);
        }

        return builder.build();
    }
}
