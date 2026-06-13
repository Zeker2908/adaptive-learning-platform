package ru.zeker.authentication.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;

@Getter
@Setter
@ConfigurationProperties(prefix = "app.demo-login")
public class DemoLoginProperties {
    private boolean enabled;
    private String email;
    private String token;
}
