package ru.zeker.solution.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import ru.zeker.common.dto.solution.request.SolutionRequest;
import ru.zeker.common.dto.solution.response.DailyActivity;
import ru.zeker.common.dto.solution.response.SolutionResponse;
import ru.zeker.common.dto.solution.response.UserProgressResponse;
import ru.zeker.solution.domain.mapper.SolutionMapper;
import ru.zeker.solution.domain.mapper.UserProgressMapper;
import ru.zeker.solution.service.SolutionService;
import ru.zeker.solution.service.UserProgressService;

import java.util.List;
import java.util.UUID;

import static ru.zeker.common.headers.ApiHeaders.USER_ID;

@Validated
@RestController
@RequestMapping("/solutions")
@RequiredArgsConstructor
@Tag(name = "Solution Controller", description = "Управление решениями задач и отслеживание прогресса пользователя")
@SecurityRequirement(name = "bearerAuth")
public class SolutionController {

    private final SolutionService solutionService;
    private final UserProgressService userProgressService;
    private final SolutionMapper solutionMapper;
    private final UserProgressMapper userProgressMapper;

    @PostMapping
    @Operation(
            summary = "Отправить решение задачи",
            description = "Принимает решение пользователя по задаче и возвращает информацию о принятом решении. " +
                    "Статус решения может быть PENDING, пока не будет обработан judging-сервисом.",
            responses = {
                    @ApiResponse(
                            responseCode = "202",
                            description = "Решение успешно отправлено на обработку",
                            content = @Content(mediaType = "application/json", schema = @Schema(implementation = SolutionResponse.class))
                    ),
                    @ApiResponse(responseCode = "400", description = "Некорректный запрос"),
                    @ApiResponse(responseCode = "401", description = "Отсутствует или неверный заголовок USER_ID")
            }
    )
    public ResponseEntity<SolutionResponse> submitSolution(
            @Parameter(description = "Уникальный идентификатор пользователя", hidden = true)
            @RequestHeader(USER_ID) @NotBlank String userId,
            @Valid @RequestBody SolutionRequest request
    ) {
        return ResponseEntity.accepted()
                .body(solutionMapper.toResponse(solutionService.submitSolution(request, userId)));
    }

    @GetMapping("/{id}")
    @Operation(
            summary = "Получить решение по идентификатору",
            description = "Возвращает детали конкретного решения пользователя по его UUID.",
            responses = {
                    @ApiResponse(
                            responseCode = "200",
                            description = "Решение успешно найдено",
                            content = @Content(mediaType = "application/json", schema = @Schema(implementation = SolutionResponse.class))
                    ),
                    @ApiResponse(responseCode = "400", description = "Некорректный формат USER_ID"),
                    @ApiResponse(responseCode = "401", description = "Отсутствует или неверный заголовок USER_ID"),
                    @ApiResponse(responseCode = "404", description = "Решение не найдено или не принадлежит пользователю")
            }
    )
    public ResponseEntity<SolutionResponse> getSolution(
            @Parameter(description = "Уникальный идентификатор пользователя", hidden = true)
            @RequestHeader(USER_ID) @NotBlank String userId,
            @Parameter(description = "Идентификатор решения", required = true, example = "123e4567-e89b-12d3-a456-556642440000")
            @PathVariable("id") UUID id
    ) {
        return ResponseEntity.ok(solutionMapper.toResponse(solutionService.getSolution(id, UUID.fromString(userId))));
    }

    @GetMapping("/user")
    @Operation(
            summary = "Получить все решения пользователя",
            description = "Возвращает список всех решений, отправленных данным пользователем.",
            responses = {
                    @ApiResponse(
                            responseCode = "200",
                            description = "Список решений успешно получен",
                            content = @Content(mediaType = "application/json", array = @ArraySchema(schema = @Schema(implementation = SolutionResponse.class)))
                    ),
                    @ApiResponse(responseCode = "400", description = "Некорректный формат USER_ID"),
                    @ApiResponse(responseCode = "401", description = "Отсутствует или неверный заголовок USER_ID")
            }
    )
    public ResponseEntity<List<SolutionResponse>> getUserSolutions(
            @Parameter(description = "Уникальный идентификатор пользователя", hidden = true)
            @RequestHeader(USER_ID) @NotBlank String userId
    ) {
        return ResponseEntity.ok(solutionService.getUserSolutions(UUID.fromString(userId))
                .stream()
                .map(solutionMapper::toResponse)
                .toList());
    }

    @GetMapping("/user/progress")
    @Operation(
            summary = "Получить прогресс пользователя по задачам",
            description = "Возвращает сводную информацию о прогрессе пользователя: решённые задачи, статистика и т.п.",
            responses = {
                    @ApiResponse(
                            responseCode = "200",
                            description = "Прогресс успешно получен",
                            content = @Content(mediaType = "application/json", array = @ArraySchema(schema = @Schema(implementation = UserProgressResponse.class)))
                    ),
                    @ApiResponse(responseCode = "400", description = "Некорректный формат USER_ID"),
                    @ApiResponse(responseCode = "401", description = "Отсутствует или неверный заголовок USER_ID")
            }
    )
    public ResponseEntity<List<UserProgressResponse>> getUserProgress(
            @Parameter(description = "Уникальный идентификатор пользователя", hidden = true)
            @RequestHeader(USER_ID) @NotBlank String userId
    ) {
        return ResponseEntity.ok(userProgressService.getUserProgress(UUID.fromString(userId))
                .stream()
                .map(userProgressMapper::toResponse)
                .toList());
    }

    @Operation(
            summary = "Получить статистику активности пользователя",
            description = """
                    Возвращает агрегированные данные о количестве решённых задач по дням за указанный период.
                    Используется для построения графика активности в личном кабинете.
                    Данные возвращаются в хронологическом порядке (от старых к новым).
                    """,
            responses = {
                    @ApiResponse(
                            responseCode = "200",
                            description = "Статистика успешно получена",
                            content = @Content(
                                    array = @ArraySchema(schema = @Schema(implementation = DailyActivity.class))
                            )
                    ),
                    @ApiResponse(responseCode = "400", description = "Некорректное значение параметра 'days' (должно быть от 1 до 30)"),
                    @ApiResponse(responseCode = "401", description = "Не указан или неверен заголовок авторизации (X-User-Id)")
            }
    )
    @GetMapping("/user/activity")
    public ResponseEntity<List<DailyActivity>> getUserActivity(
            @Parameter(description = "Уникальный идентификатор пользователя", hidden = true)
            @RequestHeader(USER_ID) String userId,

            @Parameter(
                    description = "Количество последних дней для агрегации (максимум 30)",
                    example = "14"
            )
            @RequestParam(value = "days", defaultValue = "14")
            @Min(1)
            @Max(30)
            int days
    ) {
        return ResponseEntity.ok(solutionService.getUserActivity(UUID.fromString(userId), days));
    }

}
