package ru.zeker.authentication.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import ru.zeker.authentication.domain.dto.request.GrantAdminRequest;
import ru.zeker.authentication.domain.dto.response.UserResponse;
import ru.zeker.authentication.domain.mapper.UserMapper;
import ru.zeker.authentication.service.UserService;

import java.util.UUID;

/**
 * Admin controller for managing users.
 * Provides endpoints for listing, retrieving, granting admin rights, and deleting users.
 */
@Validated
@RestController
@RequestMapping("/admin")
@RequiredArgsConstructor
@Tag(name = "Admin User Management", description = "API for administrators to manage users")
@SecurityRequirement(name = "bearerAuth")
public class AdminController {

    private final UserService userService;
    private final UserMapper userMapper;

    /**
     * Retrieves a paginated list of all users.
     *
     * @param pageable Pagination configuration (page, size, sort)
     * @return {@link ResponseEntity} with paginated list of {@link UserResponse}
     */
    @Operation(
            summary = "Get all users",
            description = "Returns a paginated list of all registered users",
            responses = {
                    @ApiResponse(
                            responseCode = "200",
                            description = "Users retrieved successfully",
                            content = @Content(schema = @Schema(implementation = Page.class)))
            }
    )
    @GetMapping("/users")
    public ResponseEntity<Page<UserResponse>> getAllUsers(
            @Parameter(description = "Pagination parameters: page (0-based), size, sort")
            @PageableDefault(size = 20) Pageable pageable) {
        Page<UserResponse> users = userService.findAll(pageable)
                .map(userMapper::toResponse);
        return ResponseEntity.ok(users);
    }

    /**
     * Retrieves a specific user by ID.
     *
     * @param userId User ID (UUID string)
     * @return {@link ResponseEntity} with user data in {@link UserResponse} format
     */
    @Operation(
            summary = "Get user by ID",
            description = "Returns detailed information about a user by their UUID",
            responses = {
                    @ApiResponse(
                            responseCode = "200",
                            description = "User found",
                            content = @Content(schema = @Schema(implementation = UserResponse.class))),
                    @ApiResponse(responseCode = "404", description = "User not found")
            }
    )
    @GetMapping("/users/{userId}")
    public ResponseEntity<UserResponse> getUserById(
            @Parameter(description = "Unique user identifier (UUID)", required = true)
            @PathVariable @NotBlank String userId) {
        return ResponseEntity.ok(userMapper.toResponse(userService.findById(UUID.fromString(userId))));
    }


    /**
     * Searches for a user by email.
     *
     * @param email User email (case-insensitive, exact match)
     * @return {@link ResponseEntity} with user data in {@link UserResponse} format
     */
    @Operation(
            summary = "Find user by email",
            description = "Returns a user matching the provided email address (exact match, case-insensitive)",
            responses = {
                    @ApiResponse(
                            responseCode = "200",
                            description = "User found",
                            content = @Content(schema = @Schema(implementation = UserResponse.class))),
                    @ApiResponse(responseCode = "404", description = "User with given email not found")
            }
    )
    @GetMapping("/users/email/{email}")
    public ResponseEntity<UserResponse> getUserByEmail(
            @Parameter(description = "User email address", required = true)
            @PathVariable @NotBlank @Email String email) {
        var user = userService.findByEmail(email);
        return ResponseEntity.ok(userMapper.toResponse(user));
    }

    /**
     * Grants admin role to a user.
     *
     * @param request {@link GrantAdminRequest} containing user ID
     * @return {@link ResponseEntity} with status code 204 (No Content)
     */
    @Operation(
            summary = "Grant admin role",
            description = "Grants ADMIN role to the specified user",
            responses = {
                    @ApiResponse(responseCode = "200", description = "Admin role granted successfully"),
                    @ApiResponse(responseCode = "400", description = "Invalid user ID"),
                    @ApiResponse(responseCode = "404", description = "User not found")
            }
    )
    @PatchMapping("/users/grant-admin")
    public ResponseEntity<UserResponse> grantAdmin(
            @Parameter(description = "User ID to grant admin role", required = true)
            @RequestBody @Valid GrantAdminRequest request) {
        return ResponseEntity.ok(userMapper.toResponse(userService.grantAdmin(request)));
    }

}