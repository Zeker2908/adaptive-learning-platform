package ru.zeker.task.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import ru.zeker.common.dto.task.request.TaskRequest;
import ru.zeker.common.dto.task.response.TaskResponse;
import ru.zeker.task.domain.mapper.TaskMapper;
import ru.zeker.task.service.TaskService;

import java.util.UUID;

@Validated
@RestController
@RequestMapping("/admin/tasks")
@RequiredArgsConstructor
@SecurityRequirement(name = "bearerAuth")
public class AdminTaskController {

    private final TaskService taskService;
    private final TaskMapper taskMapper;

    // ====================== CREATE TASK ==========================

    @Operation(
            summary = "Создать новую задачу",
            description = """
                    Создаёт новую задачу с указанным названием, описанием, шаблоном, сложностью,
                    тестами и тегами. \s
                    Возвращает созданную задачу.
                    """
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "201",
                    description = "Задача успешно создана",
                    content = @Content(schema = @Schema(implementation = TaskResponse.class))
            ),
            @ApiResponse(responseCode = "400", description = "Ошибка валидации запроса")
    })
    @PostMapping
    public ResponseEntity<TaskResponse> createTask(
            @Parameter(description = "Данные новой задачи", required = true)
            @Valid @RequestBody TaskRequest request
    ) {
        return ResponseEntity.status(201).body(taskMapper.toResponse(taskService.createTask(request)));
    }


    // ====================== UPDATE TASK ==========================

    @Operation(
            summary = "Обновить существующую задачу",
            description = """
                    Обновляет поля задачи по её ID. \s
                    Если задача не найдена — будет ошибка 404.
                    """
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "Задача успешно обновлена",
                    content = @Content(schema = @Schema(implementation = TaskResponse.class))
            ),
            @ApiResponse(responseCode = "400", description = "Ошибка валидации входных данных"),
            @ApiResponse(responseCode = "404", description = "Задача с указанным ID не найдена")
    })
    @PutMapping("/{id}")
    public ResponseEntity<TaskResponse> updateTask(
            @Parameter(description = "UUID задачи", required = true)
            @PathVariable("id") UUID id,

            @Parameter(description = "Обновлённые данные задачи", required = true)
            @Valid @RequestBody TaskRequest request
    ) {
        return ResponseEntity.ok(taskMapper.toResponse(taskService.updateTask(id, request)));
    }


    // ====================== DELETE TASK ==========================

    @Operation(
            summary = "Удалить задачу",
            description = """
                    Удаляет задачу по UUID. \s
                    Возвращает статус 204 No Content без тела.
                    """
    )
    @ApiResponses({
            @ApiResponse(responseCode = "204", description = "Задача успешно удалена"),
            @ApiResponse(responseCode = "404", description = "Задача не найдена")
    })
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteTask(
            @Parameter(description = "UUID задачи", required = true)
            @PathVariable("id") UUID id
    ) {
        taskService.deleteTask(id);
        return ResponseEntity.noContent().build();
    }
}
