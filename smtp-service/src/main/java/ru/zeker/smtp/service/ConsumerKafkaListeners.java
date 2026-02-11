package ru.zeker.smtp.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;
import ru.zeker.common.dto.kafka.smtp.EmailEvent;
import ru.zeker.common.dto.kafka.smtp.EmailEventType;
import ru.zeker.smtp.dto.EmailContext;
import ru.zeker.smtp.service.handlers.EmailContextStrategy;

import java.util.Map;
import java.util.Objects;

/**
 * Service for listening to and processing Kafka events.
 * Handles events related to user registration and smtp sending
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class ConsumerKafkaListeners {
    private final EmailService emailService;
    private final Map<EmailEventType, EmailContextStrategy> emailEventContextMap;

    /**
     * Listener for email sending events for users.
     * Processes message batches from the 'email.smtp.events' topic
     *
     * @param record list of records with email sending events for users
     */
    @KafkaListener(
            topics = "email.smtp.events",
            containerFactory = "emailKafkaListenerContainerFactory"
    )
    void listenRegisteredEvents(
            ConsumerRecord<String, EmailEvent> record
    ) {
        log.info("Message received");
        handleRecord(record);
        log.info("Event processing completed");
    }

    /**
     * Processes a single Kafka record containing an email sending event.
     * <p>
     * Performs event validation, determines processing strategy based on event type,
     * and initiates further event processing. If the event is invalid or unsupported,
     * the record is logged and no further processing occurs.
     *
     * @param record Kafka record containing {@link EmailEvent}
     */
    private void handleRecord(ConsumerRecord<String, EmailEvent> record) {
        var event = record.value();
        if (Objects.isNull(event)) {
            log.warn("Empty event in Kafka record: partition={}, offset={}", record.partition(), record.offset());
            return;
        }

        var contextStrategy = emailEventContextMap.get(event.getType());
        if (Objects.isNull(contextStrategy)) {
            log.error("Unknown event: {}", event.getType());
            return;
        }

        processEmailEvent(record, contextStrategy.handle(event));
    }

    /**
     * Processes a single email sending event
     *
     * @param record       Kafka record
     * @param emailContext context for sending email
     */
    private void processEmailEvent(
            ConsumerRecord<String, EmailEvent> record,
            EmailContext emailContext
    ) {
        var event = record.value();
        var eventType = event.getType().name();
        try {
            log.info("Processing {} event for user: {}, partition: {}, offset: {}",
                    eventType, event.getEmail(), record.partition(), record.offset());
            emailService.sendEmail(emailContext);
            log.debug("Event {} processed and asynchronous sending initiated for: {}",
                    eventType, event.getEmail());
        } catch (Exception e) {
            log.error("Error processing {} event: {}", eventType, e.getMessage(), e);
            throw e;
        }
    }

}
