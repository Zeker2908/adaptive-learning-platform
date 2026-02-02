package ru.zeker.task.controller;

import com.fasterxml.jackson.annotation.JsonView;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import ru.zeker.common.dto.task.Difficulty;
import ru.zeker.common.dto.task.json.Views;
import ru.zeker.common.dto.task.response.TaskResponse;
import ru.zeker.task.domain.mapper.TaskMapper;
import ru.zeker.task.service.TaskService;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/tasks")
@RequiredArgsConstructor
@SecurityRequirement(name = "bearerAuth")
public class TaskController {

    private final TaskService taskService;
    private final TaskMapper taskMapper;

    // ===================== GET TASK LIST ============================
    @Operation(
            summary = "Get task list",
            description = """
                    Returns a list of tasks filtered by:
                    • title — search by substring
                    • difficulty — list of difficulties (EASY, MEDIUM, HARD)
                    • tags — list of tags (task must contain all specified tags)
                    • count — maximum number of tasks to return (default 20, maximum 100)

                    Filters work together.
                    If nothing is specified — returns all tasks (with count limit).
                    """
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "Task list successfully retrieved",
                    content = @Content(array = @ArraySchema(schema = @Schema(implementation = TaskResponse.class)))
            ),
            @ApiResponse(responseCode = "400", description = "Invalid request parameters")
    })
    @GetMapping
    public ResponseEntity<List<TaskResponse>> getTasks(
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

            @Parameter(description = "Maximum number of tasks to return (default 20, maximum 100)")
            @Min(1)
            @Max(100)
            @RequestParam(value = "count", defaultValue = "20") int count
    ) {
        return ResponseEntity.ok(taskService.getTasks(title, difficulties, tags, count)
                .stream().map(taskMapper::toResponse).toList());
    }

    // ====================== GET ONE TASK ============================
    @Operation(
            summary = "Get task by ID",
            description = "Returns full information about a task by its UUID."
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "Task found",
                    content = @Content(schema = @Schema(implementation = TaskResponse.class))
            ),
            @ApiResponse(responseCode = "404", description = "Task not found")
    })
    @GetMapping("/{id}")
    public ResponseEntity<TaskResponse> getTask(
            @Parameter(description = "Task UUID", required = true)
            @PathVariable("id") UUID id
    ) {
        return ResponseEntity.ok(taskMapper.toResponse(taskService.getTask(id)));
    }

    // ====================== GET RANDOM TASKS =========================
    @Operation(
            summary = "Get random tasks",
            description = """
                    Returns a random list of tasks in the specified count. \s
                    Used for generating random practice sets.
                    """
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "Random tasks successfully retrieved",
                    content = @Content(array = @ArraySchema(schema = @Schema(implementation = TaskResponse.class)))
            ),
            @ApiResponse(responseCode = "400", description = "Invalid count parameter")
    })
    @GetMapping("/random")
    @JsonView(Views.Public.class)
    public ResponseEntity<List<TaskResponse>> getRandomTasks(
            @Parameter(description = "Number of random tasks", example = "5", required = true)
            @Min(1)
            @Max(100)
            @RequestParam(value = "count", defaultValue = "10") int count
    ) {
        return ResponseEntity.ok(taskService.getRandomTasks(count)
                .stream()
                .map(taskMapper::toResponse)
                .toList());
    }
}
