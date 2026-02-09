package ru.zeker.solution.controller;

import com.fasterxml.jackson.annotation.JsonView;
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
import jakarta.validation.constraints.NotNull;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import ru.zeker.common.dto.task.json.Views;
import ru.zeker.common.dto.task.response.TaskResponse;
import ru.zeker.solution.service.RecommendationService;

import java.util.List;
import java.util.UUID;

import static ru.zeker.common.headers.AppHeaders.USER_ID;

@RestController
@Validated
@RequestMapping("/recommendations")
@RequiredArgsConstructor
@Tag(name = "Recommendation Controller", description = "Provides personalized task recommendations based on user progress")
@SecurityRequirement(name = "bearerAuth")
public class RecommendationController {

    private final RecommendationService recommendationService;

    @Operation(
            summary = "Get recommended tasks",
            description = "Returns a list of personalized tasks recommended to the user based on their previous solutions and progress.",
            responses = {
                    @ApiResponse(
                            responseCode = "200",
                            description = "List of recommended tasks successfully retrieved",
                            content = @Content(
                                    mediaType = "application/json",
                                    array = @ArraySchema(schema = @Schema(implementation = TaskResponse.class))
                            )
                    ),
                    @ApiResponse(responseCode = "400", description = "Invalid USER_ID format"),
                    @ApiResponse(responseCode = "401", description = "Missing or invalid USER_ID header")
            }
    )
    @GetMapping
    @JsonView(Views.Public.class)
    public ResponseEntity<List<TaskResponse>> getRecommendations(
            @Parameter(description = "Unique user identifier", hidden = true)
            @RequestHeader(USER_ID) @NotNull UUID userId,

            @Parameter(
                    description = "Maximum number of tasks to return (from 1 to 10)",
                    example = "5",
                    schema = @Schema(minimum = "1", maximum = "10", defaultValue = "5")
            )
            @RequestParam(value = "limit", defaultValue = "5")
            @Min(1)
            @Max(10)
            int limit
    ) {
        return ResponseEntity.ok(recommendationService.getRecommendedTasks(userId, limit));
    }
}
