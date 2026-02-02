package ru.zeker.smtp.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import ru.zeker.common.dto.kafka.smtp.EmailEventType;
import ru.zeker.smtp.service.handlers.EmailContextStrategy;
import ru.zeker.smtp.service.handlers.ForgotPasswordEmailContextStrategy;
import ru.zeker.smtp.service.handlers.VerificationEmailContextStrategy;

import java.util.HashMap;
import java.util.Map;

@Configuration
public class EmailContextStrategyConfig {

    @Bean
    public Map<EmailEventType, EmailContextStrategy> emailEventContextMap(VerificationEmailContextStrategy emailVerificationHandler,
                                                                          ForgotPasswordEmailContextStrategy forgotPasswordHandler) {
        var emailEventContextMap = new HashMap<EmailEventType, EmailContextStrategy>();

        emailEventContextMap.put(EmailEventType.EMAIL_VERIFICATION, emailVerificationHandler);
        emailEventContextMap.put(EmailEventType.FORGOT_PASSWORD, forgotPasswordHandler);

        return emailEventContextMap;

    }
}
