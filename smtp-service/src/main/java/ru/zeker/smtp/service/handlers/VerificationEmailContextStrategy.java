package ru.zeker.smtp.service.handlers;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import ru.zeker.common.dto.kafka.smtp.EmailEvent;
import ru.zeker.smtp.dto.EmailContext;
import ru.zeker.smtp.util.ThymeleafUtils;
import ru.zeker.smtp.service.EmailService;

import java.util.Map;

@Component
@Slf4j
@RequiredArgsConstructor
public class VerificationEmailContextStrategy implements EmailContextStrategy {
    private static final String EMAIL_VERIFICATION_TEMPLATE = "email/emailVerification.html";

    private final EmailService emailService;

    @Value("${app.frontend-url:http://localhost:3000}")
    private String applicationUrl;

    @Value("${app.links.email-verification:/email-confirmation}")
    private String emailVerificationUrl;

    @Override
    public EmailContext handle(EmailEvent event) {
        log.debug("Setting up the context of the registration confirmation email: {}",
                event.getEmail());

        var verificationUrl = applicationUrl + emailVerificationUrl + "?token=" + event.getPayload().get("token");

        return emailService.createEmailContext(
                event,
                "Confirmation of registration",
                EMAIL_VERIFICATION_TEMPLATE,
                Map.of(ThymeleafUtils.ACTION_URL,verificationUrl)
        );
    }

}
