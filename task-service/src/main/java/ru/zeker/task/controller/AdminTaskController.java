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
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import ru.zeker.common.dto.task.Difficulty;
import ru.zeker.common.dto.task.request.TaskRequest;
import ru.zeker.common.dto.task.response.TaskResponse;
import ru.zeker.task.domain.mapper.TaskMapper;
import ru.zeker.task.service.TaskService;

import java.util.List;
import java.util.UUID;

@Validated
@RestController
@RequestMapping("/admin/tasks")
@RequiredArgsConstructor
@SecurityRequirement(name = "bearerAuth")
public class AdminTaskController {

    private final TaskService taskService;
    private final TaskMapper taskMapper;

    // ===================== GET TASK LIST (PAGED) ============================
    @Operation(
            summary = "Get task list (paginated)",
            description = """
                    Returns a paginated list of tasks filtered by:
                    • title — search by substring
                    • difficulty — list of difficulties (EASY, MEDIUM, HARD)
                    • tags — list of tags (task must contain all specified tags)
                    • page — page number (starts from 0)
                    • size — page size (default 20, maximum 100)
                    
                    Filters work together.
                    If nothing is specified — returns all tasks (with pagination).
                    """
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "Task list successfully retrieved",
                    content = @Content(schema = @Schema(implementation = Page.class))
            ),
            @ApiResponse(responseCode = "400", description = "Invalid request parameters")
    })
    @GetMapping
    public ResponseEntity<Page<TaskResponse>> getTasksPaged(
            @Parameter(description = "Search by task title (LIKE %title%)")
            @RequestParam(value = "title", required = false) String title,

            @Parameter(description = "List of difficulties. Example: ?difficulty=EASY&difficulty=HARD")
            @RequestParam(value = "difficulty", required = false) List<Difficulty> difficulties,

            @Parameter(description = """
                    List of tags.
                    Task must contain all specified tags.
                    Example: ?tags=Arrays&tags=Loops
                    """)
            @RequestParam(value = "tags", required = false) List<String> tags,

            @PageableDefault(size = 20) Pageable pageable
    ) {
        Page<TaskResponse> result = taskService.getTasksPaged(title, difficulties, tags, pageable)
                .map(taskMapper::toResponse);

        return ResponseEntity.ok(result);
    }

    // ====================== CREATE TASK ==========================

    @Operation(
            summary = "Create new task",
            description = """
                    Creates a new task with specified title, description, template, difficulty,
                    tests, and tags. \s
                    Returns the created task.
                    """
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "201",
                    description = "Task successfully created",
                    content = @Content(schema = @Schema(implementation = TaskResponse.class))
            ),
            @ApiResponse(responseCode = "400", description = "Request validation error")
    })
    @PostMapping
    public ResponseEntity<TaskResponse> createTask(
            @Parameter(description = "New task data", required = true)
            @Valid @RequestBody TaskRequest request
    ) {
        return ResponseEntity.status(201).body(taskMapper.toResponse(taskService.createTask(request)));
    }

    // ====================== UPDATE TASK ==========================

    @Operation(
            summary = "Update existing task",
            description = """
                    Updates task fields by its ID. \s
                    If task is not found — returns 404 error.
                    """
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "Task successfully updated",
                    content = @Content(schema = @Schema(implementation = TaskResponse.class))
            ),
            @ApiResponse(responseCode = "400", description = "Input data validation error"),
            @ApiResponse(responseCode = "404", description = "Task with specified ID not found")
    })
    @PutMapping("/{id}")
    public ResponseEntity<TaskResponse> updateTask(
            @Parameter(description = "Task UUID", required = true)
            @PathVariable("id") UUID id,

            @Parameter(description = "Updated task data", required = true)
            @Valid @RequestBody TaskRequest request
    ) {
        return ResponseEntity.ok(taskMapper.toResponse(taskService.updateTask(id, request)));
    }

    // ====================== DELETE TASK ==========================

    @Operation(
            summary = "Delete task",
            description = """
                    Deletes task by UUID. \s
                    Returns status 204 No Content without body.
                    """
    )
    @ApiResponses({
            @ApiResponse(responseCode = "204", description = "Task successfully deleted"),
            @ApiResponse(responseCode = "404", description = "Task not found")
    })
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteTask(
            @Parameter(description = "Task UUID", required = true)
            @PathVariable("id") UUID id
    ) {
        taskService.deleteTask(id);
        return ResponseEntity.noContent().build();
    }
}
