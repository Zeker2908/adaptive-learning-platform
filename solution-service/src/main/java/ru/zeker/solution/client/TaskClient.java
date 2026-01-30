package ru.zeker.solution.client;

import feign.FeignException;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.retry.annotation.Backoff;
import org.springframework.retry.annotation.Retryable;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;
import ru.zeker.common.dto.task.Difficulty;
import ru.zeker.common.dto.task.response.TaskResponse;

import java.util.List;
import java.util.UUID;

@FeignClient(name = "task-service", url = "${task.service.url:http://task-service:8082}")
public interface TaskClient {

    @Retryable(
            retryFor = {FeignException.class},
            maxAttempts = 3,
            backoff = @Backoff(delay = 1000, multiplier = 2)
    )
    @GetMapping("/tasks")
    List<TaskResponse> getTasksByTags(
            @RequestParam(value = "tags") List<String> tags,
            @RequestParam(value = "count", defaultValue = "20") int count
    );

    @Retryable(
            retryFor = {FeignException.class},
            maxAttempts = 3,
            backoff = @Backoff(delay = 1000, multiplier = 2)
    )
    @GetMapping("/tasks/{taskId}")
    TaskResponse getTaskById(@PathVariable("taskId") UUID taskId);

    @Retryable(
            retryFor = {FeignException.class},
            maxAttempts = 3,
            backoff = @Backoff(delay = 1000, multiplier = 2)
    )
    @GetMapping("/tasks/random")
    List<TaskResponse> getRandomTasks(@RequestParam(value = "count", defaultValue = "10") int count);
}