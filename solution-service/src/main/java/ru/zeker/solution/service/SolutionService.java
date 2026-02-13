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
import ru.zeker.common.dto.kafka.solution.SolutionExecResult;
import ru.zeker.common.dto.solution.SolutionStatus;
import ru.zeker.common.dto.solution.request.SolutionRequest;
import ru.zeker.common.dto.solution.response.DailyActivity;
import ru.zeker.common.dto.task.json.TaskContent;
import ru.zeker.common.dto.task.response.TaskResponse;
import ru.zeker.solution.domain.model.entity.Solution;
import ru.zeker.solution.exception.SolutionBadRequestException;
import ru.zeker.solution.exception.SolutionNotFoundException;
import ru.zeker.solution.repository.SolutionRepository;
import ru.zeker.solution.service.client.TaskClient;
import ru.zeker.solution.service.strategy.SolutionSubmissionStrategy;

import java.sql.Date;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class SolutionService {

    private final SolutionRepository repository;
    private final List<SolutionSubmissionStrategy<? extends TaskContent>> strategies;
    private final TaskClient taskClient;
    private final UserProgressService userProgressService;
    private final ObjectMapper objectMapper;

    @Transactional
    public Solution submitSolution(SolutionRequest request, UUID userId) {
        var taskResponse = taskClient.getTaskById(request.getTaskId());
        var strategy = strategies.stream()
                .filter(s -> s.support(taskResponse.getContent()))
                .findFirst()
                .orElseThrow(() -> new SolutionBadRequestException(
                        "No submission strategy available for task type: " + taskResponse.getContent().getClass().getSimpleName()
                ));

        @SuppressWarnings("unchecked")
        var typedStrategy = (SolutionSubmissionStrategy<TaskContent>) strategy;


        var solution = typedStrategy.handle(request, userId, taskResponse.getContent());

        if (solution.getStatus() != SolutionStatus.PENDING) {
            updateProgress(solution, taskResponse, solution.getStatus() == SolutionStatus.SUCCESS);
        }

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
        var solution = repository.findById(solutionId)
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
        var solution = repository.findById(solutionId)
                .orElseThrow(SolutionNotFoundException::new);

        var task = taskClient.getTaskById(solution.getTaskId());
        updateProgress(solution, task, success);
    }

    public List<DailyActivity> getUserActivity(UUID userId, long lastDays) {
        var since = LocalDateTime.now().minusDays(lastDays);
        var results = repository.findActivityByDay(userId, since);

        return results.stream()
                .map(row -> {
                    var sqlDate = (Date) row[0];
                    var localDate = sqlDate.toLocalDate();
                    var count = (Long) row[1];
                    return new DailyActivity(localDate.toString(), count);
                })
                .toList();
    }

    public long countUserSolutions(UUID userId) {
        return repository.countByUserId(userId);
    }

    public long countUserSuccessfulSolutions(UUID userId) {
        return repository.countByUserIdAndStatus(userId, SolutionStatus.SUCCESS);
    }

    @SneakyThrows
    @Scheduled(fixedDelay = 30_000)
    @Transactional
    public void checkAndTimeoutStaleSolutions() {
        var timeoutThreshold = LocalDateTime.now().minusMinutes(2);

        var staleSolutions = repository
                .findByStatusAndCreatedAtBefore(SolutionStatus.PENDING, timeoutThreshold);

        if (!staleSolutions.isEmpty()) {
            log.info("Found {} stale PENDING solutions to mark as TIMEOUT", staleSolutions.size());

            for (var solution : staleSolutions) {
                solution.setStatus(SolutionStatus.TIMEOUT);
                solution.setFeedback(objectMapper.writeValueAsString("Execution did not complete in time (timeout)"));
            }

            repository.saveAll(staleSolutions);
            log.info("Marked {} solutions as TIMEOUT", staleSolutions.size());
        }
    }

    private void updateProgress(Solution solution, TaskResponse task, boolean success) {
        var difficulty = task.getDifficulty().getRating();
        var tagCount = task.getTags().size();

        for (var topic : task.getTags()) {
            userProgressService.updateOrCreate(topic, solution.getUserId(), difficulty, success, tagCount);
        }
    }

    private boolean isOwner(Solution solution, UUID userId) {
        return solution.getUserId().equals(userId);
    }
}
