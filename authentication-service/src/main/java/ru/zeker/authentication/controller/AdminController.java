package ru.zeker.authentication.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import ru.zeker.authentication.domain.dto.response.AdminUserResponse;
import ru.zeker.authentication.domain.dto.response.UserResponse;
import ru.zeker.authentication.domain.mapper.UserMapper;
import ru.zeker.authentication.service.RefreshTokenService;
import ru.zeker.authentication.service.UserService;

import java.util.List;
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
    private final RefreshTokenService refreshTokenService;

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
    public ResponseEntity<Page<AdminUserResponse>> getAllUsers(
            @Parameter(description = "Pagination parameters: page (0-based), size, sort")
            @PageableDefault(size = 20) Pageable pageable) {
        var users = userService.findAll(pageable)
                .map(userMapper::toAdminResponse);
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
    public ResponseEntity<AdminUserResponse> getUserById(
            @Parameter(description = "Unique user identifier (UUID)", required = true)
            @PathVariable("userId") @NotNull UUID userId) {
        return ResponseEntity.ok(userMapper.toAdminResponse(userService.findById(userId)));
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
    public ResponseEntity<AdminUserResponse> getUserByEmail(
            @Parameter(description = "User email address", required = true)
            @PathVariable("email") @NotBlank @Email String email) {
        var user = userService.findByEmail(email);
        return ResponseEntity.ok(userMapper.toAdminResponse(user));
    }

    /**
     * Searches users by email prefix (autocomplete).
     *
     * @param prefix Email prefix to search for (case-insensitive)
     * @param limit Maximum number of results (default: 10)
     * @return {@link ResponseEntity} with list of matching users
     */
    @Operation(
            summary = "Search users by email prefix",
            description = "Returns users whose email starts with the given prefix (case-insensitive, autocomplete)",
            responses = {
                    @ApiResponse(
                            responseCode = "200",
                            description = "Search results",
                            content = @Content(array = @ArraySchema(schema = @Schema(implementation = UserResponse.class)))
                    )
            }
    )
    @GetMapping("/users/search")
    public ResponseEntity<List<AdminUserResponse>> searchUsersByEmail(
            @Parameter(description = "Email prefix to search for", required = true, example = "john")
            @RequestParam("q") @NotBlank String prefix,

            @Parameter(description = "Maximum number of results", example = "10")
            @RequestParam(value = "limit", defaultValue = "10") @Min(1) @Max(50) int limit
    ) {
        var users = userService.searchByEmailPrefix(prefix, limit)
                .stream()
                .map(userMapper::toAdminResponse)
                .toList();
        return ResponseEntity.ok(users);
    }

    /**
     * Grants admin role to a user.
     *
     * @param userId {@link UUID} user ID
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
    @PatchMapping("/users/{userId}/grant-admin")
    public ResponseEntity<AdminUserResponse> grantAdmin(
            @Parameter(description = "User ID to grant admin role", required = true)
            @PathVariable("userId") @NotNull UUID userId) {
        return ResponseEntity.ok(userMapper.toAdminResponse(userService.grantAdmin(userId)));
    }

    /**
     * Blocks a user.
     *
     * @param userId ID of the user to block
     * @return {@link ResponseEntity} with status code 204 (No Content)
     */
    @Operation(
            summary = "Block user",
            description = "Blocks the specified user and revokes all active refresh tokens",
            responses = {
                    @ApiResponse(responseCode = "200", description = "User blocked successfully"),
                    @ApiResponse(responseCode = "400", description = "Invalid user ID"),
                    @ApiResponse(responseCode = "404", description = "User not found")
            }
    )
    @PatchMapping("/users/{userId}/block")
    public ResponseEntity<AdminUserResponse> blockUser(
            @Parameter(description = "User ID to block", required = true)
            @PathVariable("userId") @NotNull UUID userId
    ) {
        var response = userMapper.toAdminResponse(userService.updateUserBlocked(userId, true));
        refreshTokenService.revokeAllUserTokens(userId);
        return ResponseEntity.ok(response);
    }

    /**
     * Unblock a user.
     *
     * @param userId ID of the user to Unblock
     * @return {@link ResponseEntity} with status code 204 (No Content)
     */
    @Operation(
            summary = "Unblock user",
            description = "Unblock the specified user and revokes all active refresh tokens",
            responses = {
                    @ApiResponse(responseCode = "200", description = "User unblock successfully"),
                    @ApiResponse(responseCode = "400", description = "Invalid user ID"),
                    @ApiResponse(responseCode = "404", description = "User not found")
            }
    )
    @PatchMapping("/users/{userId}/unblock")
    public ResponseEntity<AdminUserResponse> unblockUser(
            @Parameter(description = "User ID to unblock", required = true)
            @PathVariable("userId") @NotNull UUID userId
    ) {
        return ResponseEntity.ok(userMapper.toAdminResponse(userService.updateUserBlocked(userId, false)));
    }

}