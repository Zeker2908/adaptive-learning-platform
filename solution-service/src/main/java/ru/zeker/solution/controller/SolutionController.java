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
import jakarta.validation.constraints.NotNull;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
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

import static ru.zeker.common.headers.AppHeaders.USER_ID;

@Validated
@RestController
@RequestMapping("/solutions")
@RequiredArgsConstructor
@Tag(name = "Solution Controller", description = "Managing task solutions and tracking user progress")
@SecurityRequirement(name = "bearerAuth")
public class SolutionController {

    private final SolutionService solutionService;
    private final UserProgressService userProgressService;
    private final SolutionMapper solutionMapper;
    private final UserProgressMapper userProgressMapper;

    @PostMapping
    @Operation(
            summary = "Submit task solution",
            description = "Accepts user solution for a task and returns information about the submitted solution. " +
                    "Solution status can be PENDING until processed by judging service.",
            responses = {
                    @ApiResponse(
                            responseCode = "200",
                            description = "Solution successfully submitted or sent for processing",
                            content = @Content(mediaType = "application/json", schema = @Schema(implementation = SolutionResponse.class))
                    ),
                    @ApiResponse(responseCode = "400", description = "Invalid request"),
                    @ApiResponse(responseCode = "401", description = "Missing or invalid USER_ID header")
            }
    )
    public ResponseEntity<SolutionResponse> submitSolution(
            @Parameter(description = "Unique user identifier", hidden = true)
            @RequestHeader(USER_ID) @NotNull UUID userId,
            @Valid @RequestBody SolutionRequest request
    ) {
        return ResponseEntity.ok(solutionMapper.toResponse(solutionService.submitSolution(request, userId)));
    }

    @GetMapping("/{id}")
    @Operation(
            summary = "Get solution by identifier",
            description = "Returns details of a specific user solution by its UUID.",
            responses = {
                    @ApiResponse(
                            responseCode = "200",
                            description = "Solution successfully found",
                            content = @Content(mediaType = "application/json", schema = @Schema(implementation = SolutionResponse.class))
                    ),
                    @ApiResponse(responseCode = "400", description = "Invalid USER_ID format"),
                    @ApiResponse(responseCode = "401", description = "Missing or invalid USER_ID header"),
                    @ApiResponse(responseCode = "404", description = "Solution not found or does not belong to user")
            }
    )
    public ResponseEntity<SolutionResponse> getSolution(
            @Parameter(description = "Unique user identifier", hidden = true)
            @RequestHeader(USER_ID) @NotNull UUID userId,
            @Parameter(description = "Solution identifier", required = true, example = "123e4567-e89b-12d3-a456-556642440000")
            @PathVariable("id") UUID id
    ) {
        return ResponseEntity.ok(solutionMapper.toResponse(solutionService.getSolution(id, userId)));
    }

    @GetMapping("/user")
    @Operation(
            summary = "Get all user solutions",
            description = "Returns a list of all solutions submitted by the given user.",
            responses = {
                    @ApiResponse(
                            responseCode = "200",
                            description = "List of solutions successfully retrieved",
                            content = @Content(mediaType = "application/json", array = @ArraySchema(schema = @Schema(implementation = SolutionResponse.class)))
                    ),
                    @ApiResponse(responseCode = "400", description = "Invalid USER_ID format"),
                    @ApiResponse(responseCode = "401", description = "Missing or invalid USER_ID header")
            }
    )
    public ResponseEntity<List<SolutionResponse>> getUserSolutions(
            @Parameter(description = "Unique user identifier", hidden = true)
            @RequestHeader(USER_ID) @NotNull UUID userId
    ) {
        return ResponseEntity.ok(solutionService.getUserSolutions(userId)
                .stream()
                .map(solutionMapper::toResponse)
                .toList());
    }

    @GetMapping("/user/progress")
    @Operation(
            summary = "Get user progress on tasks",
            description = "Returns summary information about user progress: solved tasks, statistics, etc.",
            responses = {
                    @ApiResponse(
                            responseCode = "200",
                            description = "Progress successfully retrieved",
                            content = @Content(mediaType = "application/json", array = @ArraySchema(schema = @Schema(implementation = UserProgressResponse.class)))
                    ),
                    @ApiResponse(responseCode = "400", description = "Invalid USER_ID format"),
                    @ApiResponse(responseCode = "401", description = "Missing or invalid USER_ID header")
            }
    )
    public ResponseEntity<Page<UserProgressResponse>> getUserProgress(
            @Parameter(description = "Unique user identifier", hidden = true)
            @RequestHeader(USER_ID) @NotNull UUID userId,
            @PageableDefault(size = 20) Pageable pageable
    ) {
        return ResponseEntity.ok(userProgressService.getUserProgress(userId, pageable)
                .map(userProgressMapper::toResponse));
    }

    @Operation(
            summary = "Get user activity statistics",
            description = """
                    Returns aggregated data on the number of solved tasks per day for the specified period.
                    Used for building activity charts in the personal dashboard.
                    Data is returned in chronological order (oldest to newest).
                    """,
            responses = {
                    @ApiResponse(
                            responseCode = "200",
                            description = "Statistics successfully retrieved",
                            content = @Content(
                                    array = @ArraySchema(schema = @Schema(implementation = DailyActivity.class))
                            )
                    ),
                    @ApiResponse(responseCode = "400", description = "Invalid value for 'days' parameter (must be between 1 and 30)"),
                    @ApiResponse(responseCode = "401", description = "Missing or invalid authorization header (X-User-Id)")
            }
    )
    @GetMapping("/user/activity")
    public ResponseEntity<List<DailyActivity>> getUserActivity(
            @Parameter(description = "Unique user identifier", hidden = true)
            @RequestHeader(USER_ID) @NotNull UUID userId,

            @Parameter(
                    description = "Number of recent days to aggregate (maximum 30)",
                    example = "14"
            )
            @RequestParam(value = "days", defaultValue = "14")
            @Min(1)
            @Max(90)
            long days
    ) {
        return ResponseEntity.ok(solutionService.getUserActivity(userId, days));
    }

}
