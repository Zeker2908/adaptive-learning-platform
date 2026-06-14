package ru.zeker.smtp.service;

import jakarta.mail.MessagingException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.eclipse.angus.mail.smtp.SMTPSenderFailedException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.FileSystemResource;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;
import org.thymeleaf.context.Context;
import org.thymeleaf.spring6.SpringTemplateEngine;
import ru.zeker.common.dto.kafka.smtp.EmailEvent;
import ru.zeker.smtp.dto.EmailContext;
import ru.zeker.smtp.exception.EmailSendingException;
import ru.zeker.smtp.util.ThymeleafUtils;

import java.nio.charset.StandardCharsets;
import java.time.Year;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;

/**
 * Service for sending emails using Thymeleaf templates
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class EmailService {
    private final JavaMailSender javaMailSender;
    private final SpringTemplateEngine springTemplateEngine;

    @Value("${spring.mail.username}")
    private String from;

    @Value("${app.company-name:AdaptCode}")
    private String companyName;

    public void sendEmail(EmailContext emailContext) {
        log.info("Preparing to send email to: {}", emailContext.getTo());

        try {
            var message = javaMailSender.createMimeMessage();
            var messageHelper = new MimeMessageHelper(
                    message,
                    MimeMessageHelper.MULTIPART_MODE_MIXED_RELATED,
                    StandardCharsets.UTF_8.name()
            );

            var thymeleafContext = new Context();
            thymeleafContext.setVariables(emailContext.getTemplateContext());

            var htmlContent = springTemplateEngine.process(
                    emailContext.getTemplateLocation(),
                    thymeleafContext
            );

            var senderName = Optional.ofNullable(emailContext.getFromDisplayName())
                            .orElse(companyName);

            messageHelper.setFrom(emailContext.getFrom(), senderName);
            messageHelper.setTo(emailContext.getTo());
            messageHelper.setSubject(emailContext.getSubject());
            messageHelper.setText(htmlContent, true);
            if (Objects.nonNull(emailContext.getAttachment())) {
                var file = new FileSystemResource(emailContext.getAttachment());
                messageHelper.addAttachment(file.getFilename(), file);
            }

            log.info("Sending email with subject '{}' to: {}",
                    emailContext.getSubject(), emailContext.getTo());

            javaMailSender.send(message);

            log.info("Email successfully sent to: {}", emailContext.getTo());

        } catch (SMTPSenderFailedException e) {
            log.error("Error sending email to {}: {}",
                    emailContext.getTo(), e.getMessage(), e);
            throw new EmailSendingException("Error sending email: " + e.getMessage());
        } catch (MessagingException e) {
            log.error("Error preparing email for {}: {}",
                    emailContext.getTo(), e.getMessage(), e);
            throw new EmailSendingException("Error preparing email: " + e.getMessage());
        } catch (Exception e) {
            log.error("Unexpected error sending email to {}: {}",
                    emailContext.getTo(), e.getMessage(), e);
            throw new EmailSendingException("Error sending email: " + e.getMessage());
        }
    }

    /**
     * General method for creating email context based on an event
     *
     * @param event            event triggering email sending
     * @param subject          email subject
     * @param templateLocation path to email template
     * @param payloadContext   payload (registration confirmation links, password reset links, etc.)
     * @return configured context for sending email
     */
    public EmailContext createEmailContext(
            EmailEvent event,
            String subject,
            String templateLocation,
            Map<String, Object> payloadContext
    ) {
        var templateContext = new HashMap<String, Object>();
        templateContext.put(ThymeleafUtils.CURRENT_YEAR, Year.now().getValue());
        templateContext.put(ThymeleafUtils.COMPANY_NAME, companyName);
        if (Objects.nonNull(payloadContext)) {
            templateContext.putAll(payloadContext);
        }

        return EmailContext.builder()
                .from(from)
                .to(event.getEmail())
                .subject(subject)
                .emailLanguage("ru")
                .templateLocation(templateLocation)
                .templateContext(templateContext)
                .build();
    }
}
