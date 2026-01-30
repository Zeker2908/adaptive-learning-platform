package ru.zeker.solution.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import ru.zeker.common.dto.task.response.TaskResponse;
import ru.zeker.solution.service.RecommendationService;

import java.util.List;
import java.util.UUID;

import static ru.zeker.common.headers.ApiHeaders.USER_ID;

@RestController
@Validated
@RequestMapping("/recommendations")
@RequiredArgsConstructor
@Tag(name = "Recommendation Controller", description = "Предоставляет персонализированные рекомендации задач на основе прогресса пользователя")
@SecurityRequirement(name = "bearerAuth")
public class RecommendationController {

    private final RecommendationService recommendationService;

    @GetMapping
    @Operation(
            summary = "Получить рекомендованные задачи",
            description = "Возвращает список персонализированных задач, рекомендованных пользователю на основе его предыдущих решений и прогресса.",
            responses = {
                    @ApiResponse(
                            responseCode = "200",
                            description = "Список рекомендованных задач успешно получен",
                            content = @Content(
                                    mediaType = "application/json",
                                    array = @ArraySchema(schema = @Schema(implementation = TaskResponse.class))
                            )
                    ),
                    @ApiResponse(responseCode = "400", description = "Некорректный формат USER_ID"),
                    @ApiResponse(responseCode = "401", description = "Отсутствует или неверный заголовок USER_ID")
            }
    )
    public ResponseEntity<List<TaskResponse>> getRecommendations(
            @Parameter(description = "Уникальный идентификатор пользователя", hidden = true)
            @RequestHeader(USER_ID) String userId,

            @Parameter(
                    description = "Максимальное количество возвращаемых задач (от 1 до 10)",
                    example = "5",
                    schema = @Schema(minimum = "1", maximum = "10", defaultValue = "5")
            )
            @RequestParam(value = "limit", defaultValue = "5")
            @Min(1)
            @Max(10)
            int limit
    ) {
        List<TaskResponse> tasks = recommendationService.getRecommendedTasks(UUID.fromString(userId), limit);
        return ResponseEntity.ok(tasks);
    }
}