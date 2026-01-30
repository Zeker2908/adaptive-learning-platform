package ru.zeker.solution.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;
import ru.zeker.common.dto.kafka.solution.SolutionExecResult;
import ru.zeker.common.dto.solution.SolutionStatus;

import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class ConsumerKafkaListeners {

    private final SolutionService solutionService;

    @KafkaListener(
            topics = "solution.exec.result",
            containerFactory = "solutionExecKafkaListenerContainerFactory"
    )
    void listen(
            ConsumerRecord<String, SolutionExecResult> record
    ) {
        try {
            log.info("Message received");
            log.info("record {}", record);
            handleMessage(record);
            log.info("Message processing completed");
        } catch (Exception e) {
            log.error("Failed to process Kafka message (offset={}, partition={}), error: {}",
                    record.offset(), record.partition(), e.getMessage(), e);
        }
    }

    private void handleMessage(ConsumerRecord<String, SolutionExecResult> record) throws JsonProcessingException {
        SolutionExecResult result = record.value();
        UUID solutionId = parseSolutionIdOrThrow(result.getSolutionId());
        SolutionStatus status = result.getStatus();
        solutionService.updateSolutionStatus(solutionId, result);
        if (shouldUpdateProgress(status)) {
            solutionService.updateProgressIfNeeded(solutionId, result.getStatus() == SolutionStatus.SUCCESS);
        }
    }

    private boolean shouldUpdateProgress(SolutionStatus status) {
        return status != SolutionStatus.SERVICE_UNAVAILABLE;
    }

    private UUID parseSolutionIdOrThrow(String idStr) {
        try {
            return UUID.fromString(idStr);
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException("Invalid solution ID format: " + idStr, e);
        }
    }
}
