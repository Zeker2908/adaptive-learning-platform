package ru.zeker.sandbox.service;

import feign.FeignException;
import feign.RetryableException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import ru.zeker.common.dto.judge0.response.SubmissionResponse;
import ru.zeker.common.dto.kafka.solution.SolutionExecRequest;
import ru.zeker.common.dto.kafka.solution.SolutionExecResult;
import ru.zeker.common.dto.solution.SolutionStatus;
import ru.zeker.sandbox.exception.CodeExecutionException;

import java.util.Objects;

@Slf4j
@Service
@RequiredArgsConstructor
public class ConsumerKafkaListeners {

    private final KafkaProducer kafkaProducer;
    private final CodeExecutionService codeExecutionService;

    @Async("virtualThead") // Чтобы не блокировать 16 потоков у консюмера при ожидании выполнении кода
    @KafkaListener(
            topics = "solution.exec.request",
            containerFactory = "solutionExecKafkaListenerContainerFactory"
    )
    void listen(
            ConsumerRecord<String, SolutionExecRequest> record
    ) {
        try {
            log.info("Message {}", record);
            SubmissionResponse response = codeExecutionService.execute(record.value());
            if (!isValidStatus(response)) {
                log.error("response {}", response);
                throw new CodeExecutionException("Execution failed: " + Objects.requireNonNullElse(response.getMessage(), response.getStatus().getDescription()));
            }
            log.info("Result {}", response);
            SolutionExecResult solutionExecResult = SolutionExecResult.builder()
                    .solutionId(record.value().getSolutionId())
                    .status(SolutionStatus.SUCCESS)
                    .build();
            kafkaProducer.sendEmailEvent(solutionExecResult);
            log.info("Message processing completed");
        } catch (RetryableException | FeignException.ServiceUnavailable e) {
            log.error("Judge0 service is temporarily unavailable: {}", e.getMessage(), e);
            SolutionExecResult solutionExecResult = SolutionExecResult.builder()
                    .solutionId(record.value().getSolutionId())
                    .status(SolutionStatus.SERVICE_UNAVAILABLE)
                    .descriptionError("Execution service is temporarily unavailable")
                    .build();
            kafkaProducer.sendEmailEvent(solutionExecResult);
        } catch (CodeExecutionException e) {
            log.warn("Code execution failed: {}", e.getMessage());
            SolutionExecResult solutionExecResult = SolutionExecResult.builder()
                    .solutionId(record.value().getSolutionId())
                    .status(SolutionStatus.FAILED)
                    .descriptionError(e.getMessage())
                    .build();
            kafkaProducer.sendEmailEvent(solutionExecResult);
        } catch (Exception e) {
            log.error("Error while request to judge0 {}", e.getMessage(), e);
            SolutionExecResult solutionExecResult = SolutionExecResult.builder()
                    .solutionId(record.value().getSolutionId())
                    .status(SolutionStatus.FAILED)
                    .descriptionError(e.getMessage())
                    .build();
            kafkaProducer.sendEmailEvent(solutionExecResult);
        }
    }

    private boolean isValidStatus(SubmissionResponse response) {
        return response.getStatus().getId().equals(3);
    }

}
