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
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import ru.zeker.common.dto.solution.response.DailyActivity;
import ru.zeker.common.dto.solution.response.UserProgressResponse;
import ru.zeker.solution.domain.mapper.UserProgressMapper;
import ru.zeker.solution.service.SolutionService;
import ru.zeker.solution.service.UserProgressService;

import java.util.List;
import java.util.UUID;

@Validated
@RestController
@RequestMapping("/admin/solutions")
@RequiredArgsConstructor
@Tag(name = "Solution Admin Controller", description = "Get User information")
@SecurityRequirement(name = "bearerAuth")
public class SolutionAdminController {

    private final SolutionService solutionService;
    private final UserProgressService userProgressService;
    private final UserProgressMapper userProgressMapper;

    @GetMapping("/user/progress/{userId}")
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
                    @ApiResponse(responseCode = "404", description = "User not found"),
                    @ApiResponse(responseCode = "401", description = "Unauthorized")
            }
    )
    public ResponseEntity<Page<UserProgressResponse>> getUserProgress(
            @Parameter(description = "Unique user identifier")
            @PathVariable("userId") UUID userId,
            @PageableDefault(size = 20) Pageable pageable
    ) {
        return ResponseEntity.ok(userProgressService.getUserProgress(userId, pageable)
                .map(userProgressMapper::toResponse));
    }

    @GetMapping("/user/activity/{userId}")
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
                    @ApiResponse(responseCode = "404", description = "User not found"),
                    @ApiResponse(responseCode = "401", description = "Unauthorized")
            }
    )
    public ResponseEntity<List<DailyActivity>> getUserActivity(
            @Parameter(description = "Unique user identifier")
            @PathVariable("userId") UUID userId,
            @Parameter(
                    description = "Number of recent days to aggregate (maximum 90)",
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
