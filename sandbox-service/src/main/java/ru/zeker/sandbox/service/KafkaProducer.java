package ru.zeker.sandbox.service;

import lombok.RequiredArgsConstructor;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;
import org.springframework.validation.annotation.Validated;
import ru.zeker.common.dto.kafka.solution.SolutionExecResult;

@Service
@RequiredArgsConstructor
@Validated
public class KafkaProducer {
    private final KafkaTemplate<String, Object> kafkaTemplate;

    public void sendEmailEvent(SolutionExecResult message) {
        kafkaTemplate.send("solution.exec.result", message.getSolutionId(), message);
    }

}
