package ru.zeker.solution.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import ru.zeker.common.dto.kafka.solution.SolutionExecRequest;
import ru.zeker.common.dto.kafka.solution.SolutionExecResult;
import ru.zeker.common.dto.solution.SolutionStatus;
import ru.zeker.common.dto.solution.request.SolutionRequest;
import ru.zeker.common.dto.solution.response.DailyActivity;
import ru.zeker.common.dto.task.response.TaskResponse;
import ru.zeker.solution.client.TaskClient;
import ru.zeker.solution.domain.mapper.SolutionMapper;
import ru.zeker.solution.domain.model.entity.Solution;
import ru.zeker.solution.exception.SolutionNotFoundException;
import ru.zeker.solution.repository.SolutionRepository;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class SolutionService {

    private final SolutionRepository repository;
    private final KafkaProducer kafkaProducer;
    private final SolutionMapper solutionMapper;
    private final TaskClient taskClient;
    private final UserProgressService userProgressService;
    private final ObjectMapper objectMapper;

    @Transactional
    public Solution submitSolution(SolutionRequest request, String userId) {
        Solution solution = Solution.builder()
                .userId(UUID.fromString(userId))
                .taskId(request.getTaskId())
                .code(request.getCode())
                .language(request.getLanguage())
                .status(SolutionStatus.PENDING)
                .build();
        solution = repository.save(solution);

        TaskResponse taskResponse = taskClient.getTaskById(solution.getTaskId());
        SolutionExecRequest message = solutionMapper.toKafkaMessage(solution, taskResponse.getTests());
        kafkaProducer.sendEmailEvent(message);
        return solution;
    }

    public Solution getSolution(UUID id, UUID userId) {
        return repository.findById(id)
                .filter(s -> isOwner(s, userId))
                .orElseThrow(SolutionNotFoundException::new);
    }

    public List<Solution> getUserSolutions(UUID userId) {
        return repository.findByUserId(userId);
    }

    @Transactional
    public void updateSolutionStatus(UUID solutionId, SolutionExecResult result) throws JsonProcessingException {
        Solution solution = repository.findById(solutionId)
                .orElseThrow(SolutionNotFoundException::new);

        if (solution.getStatus() != SolutionStatus.PENDING) {
            log.warn("Attempt to update non-PENDING solution id={}, currentStatus={}, newStatus={}",
                    solutionId, solution.getStatus(), result.getStatus());
            return;
        }

        solution.setStatus(result.getStatus());
        if (StringUtils.isNotBlank(result.getDescriptionError())) {
            solution.setFeedback(objectMapper.writeValueAsString(result.getDescriptionError()));
        }
        repository.save(solution);
    }

    @Transactional
    public void updateProgressIfNeeded(UUID solutionId, boolean success) {
        Solution solution = repository.findById(solutionId)
                .orElseThrow(SolutionNotFoundException::new);

        TaskResponse task = taskClient.getTaskById(solution.getTaskId());
        double difficulty = task.getDifficulty().getRating();
        int tagCount = task.getTags().size();

        for (String topic : task.getTags()) {
            userProgressService.updateOrCreate(topic, solution.getUserId(), difficulty, success, tagCount);
        }
    }

    public List<DailyActivity> getUserActivity(UUID userId, int lastDays) {
        LocalDateTime since = LocalDateTime.now().minusDays(lastDays);
        List<Object[]> results = repository.findActivityByDay(userId, since);

        return results.stream()
                .map(row -> {
                    java.sql.Date sqlDate = (java.sql.Date) row[0];
                    LocalDate localDate = sqlDate.toLocalDate();
                    long count = (Long) row[1];
                    return new DailyActivity(localDate.toString(), (int) count);
                })
                .toList();
    }

    @SneakyThrows
    @Scheduled(fixedDelay = 30_000)
    @Transactional
    public void checkAndTimeoutStaleSolutions() {
        LocalDateTime timeoutThreshold = LocalDateTime.now().minusMinutes(2);

        List<Solution> staleSolutions = repository
                .findByStatusAndCreatedAtBefore(SolutionStatus.PENDING, timeoutThreshold);

        if (!staleSolutions.isEmpty()) {
            log.info("Found {} stale PENDING solutions to mark as TIMEOUT", staleSolutions.size());

            for (Solution solution : staleSolutions) {
                solution.setStatus(SolutionStatus.TIMEOUT);
                solution.setFeedback(objectMapper.writeValueAsString("Execution did not complete in time (timeout)"));
            }

            repository.saveAll(staleSolutions);
            log.info("Marked {} solutions as TIMEOUT", staleSolutions.size());
        }
    }

    private boolean isOwner(Solution solution, UUID userId) {
        return solution.getUserId().equals(userId);
    }
}
