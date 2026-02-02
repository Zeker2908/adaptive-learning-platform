package ru.zeker.smtp.service.handlers;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import ru.zeker.common.dto.kafka.smtp.EmailEvent;
import ru.zeker.smtp.dto.EmailContext;
import ru.zeker.smtp.service.EmailService;
import ru.zeker.smtp.util.ThymeleafUtils;

import java.util.Map;

@Component
@Slf4j
@RequiredArgsConstructor
public class ForgotPasswordEmailContextStrategy implements EmailContextStrategy {
    private static final String FORGOT_PASSWORD_TEMPLATE = "email/forgotPassword.html";

    private final EmailService emailService;

    @Value("${app.frontend-url:http://localhost:3000}")
    private String applicationUrl;

    @Value("${app.links.password-reset:/password-reset}")
    private String passwordResetUrl;


    @Override
    public EmailContext handle(EmailEvent event) {
        log.debug("Setting up the context of a password recovery email: {}",
                event.getEmail());

        var resetPasswordUrl = applicationUrl + passwordResetUrl + "?token=" + event.getPayload().get("token");

        return emailService.createEmailContext(
                event,
                "Password recovery",
                FORGOT_PASSWORD_TEMPLATE,
                Map.of(ThymeleafUtils.ACTION_URL, resetPasswordUrl)
        );
    }
}
