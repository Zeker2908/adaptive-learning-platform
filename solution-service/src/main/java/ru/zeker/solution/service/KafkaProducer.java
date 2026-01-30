package ru.zeker.solution.service;

import lombok.RequiredArgsConstructor;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;
import org.springframework.validation.annotation.Validated;
import ru.zeker.common.dto.kafka.solution.SolutionExecRequest;

@Service
@RequiredArgsConstructor
@Validated
public class KafkaProducer {
    private final KafkaTemplate<String, Object> kafkaTemplate;

    public void sendEmailEvent(SolutionExecRequest message) {
        kafkaTemplate.send("solution.exec.request", message.getSolutionId(), message);
    }

}
