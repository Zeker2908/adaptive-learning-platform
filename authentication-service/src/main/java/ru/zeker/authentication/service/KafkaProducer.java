package ru.zeker.authentication.service;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;
import org.springframework.validation.annotation.Validated;
import ru.zeker.common.dto.kafka.smtp.EmailEvent;

@Service
@RequiredArgsConstructor
@Validated
public class KafkaProducer {
    private final KafkaTemplate<String, Object> kafkaTemplate;

    public void sendEmailEvent(@Valid EmailEvent event) {
        kafkaTemplate.send("email.smtp.events", event.getId(), event);
    }

}
