package ru.zeker.authentication.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.ws.rs.core.HttpHeaders;
import lombok.RequiredArgsConstructor;
import org.apache.commons.lang3.StringUtils;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.CookieValue;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import ru.zeker.authentication.domain.dto.request.BindPasswordRequest;
import ru.zeker.authentication.domain.dto.request.ChangePasswordRequest;
import ru.zeker.authentication.domain.dto.response.UserResponse;
import ru.zeker.authentication.domain.mapper.UserMapper;
import ru.zeker.authentication.service.RefreshTokenService;
import ru.zeker.authentication.service.UserService;
import ru.zeker.authentication.util.CookieUtils;

import java.time.Duration;
import java.util.UUID;

import static ru.zeker.common.headers.AppHeaders.USER_ID;

/**
 * Controller for managing users and their authentication data.
 * Provides operations for retrieving user information, managing passwords, and deleting accounts.
 */
@Validated
@RestController
@RequestMapping("/users")
@RequiredArgsConstructor
@Tag(name = "User Management", description = "API for managing users and their authentication data")
@SecurityRequirement(name = "bearerAuth")
public class UserController {
    private final UserService userService;
    private final UserMapper userMapper;
    private final RefreshTokenService refreshTokenService;

    /**
     * Retrieves information about the current authenticated user.
     *
     * @param id User ID passed in the request header (required, non-empty)
     * @return {@link ResponseEntity} with user data in {@link UserResponse} format
     * @throws jakarta.validation.ConstraintViolationException if ID is empty or invalid
     */
    @Operation(
            summary = "Get user information",
            description = "Returns data of the current authenticated user",
            responses = {
                    @ApiResponse(
                            responseCode = "200",
                            description = "User data retrieved successfully",
                            content = @Content(schema = @Schema(implementation = UserResponse.class)))
            }
    )
    @GetMapping("/me")
    public ResponseEntity<UserResponse> getCurrentUser(
            @Parameter(description = "Unique user identifier", hidden = true)
            @RequestHeader(USER_ID) @NotBlank String id) {
        return ResponseEntity.ok(userMapper.toResponse(userService.findById(UUID.fromString(id))));
    }

    /**
     * Binds a password to the user account.
     *
     * @param id      User ID passed in the request header (required, non-empty)
     * @param request {@link BindPasswordRequest} with data for binding password
     * @return {@link ResponseEntity} with status code 202 (Accepted)
     * @throws jakarta.validation.ConstraintViolationException if ID or request data is invalid
     */
    @Operation(
            summary = "Bind password",
            description = "Binds a password to the user account",
            responses = {
                    @ApiResponse(responseCode = "202", description = "Password binding request accepted"),
                    @ApiResponse(responseCode = "400", description = "Invalid input data"),
                    @ApiResponse(responseCode = "409", description = "Password already bound")
            }
    )
    @PutMapping("/me/password")
    public ResponseEntity<Void> bindPassword(
            @Parameter(description = "Unique user identifier", hidden = true)
            @RequestHeader(USER_ID) @NotBlank String id,

            @Parameter(description = "Data for binding password", required = true)
            @RequestBody @Valid BindPasswordRequest request) {
        userService.bindPassword(id, request);
        return ResponseEntity.accepted().build();
    }

    /**
     * Changes user password and logs out from all devices.
     *
     * @param id                     User ID passed in the request header (required, non-empty)
     * @param changePasswordRequest {@link ChangePasswordRequest} with current and new password
     * @param refreshToken           Refresh token from cookie (required, non-empty)
     * @param response               {@link HttpServletResponse} for clearing cookies
     * @return {@link ResponseEntity} with status code 204 (No Content)
     * @throws jakarta.validation.ConstraintViolationException if parameters are invalid
     */
    @Operation(
            summary = "Change password",
            description = "Changes user password and logs out from all devices",
            responses = {
                    @ApiResponse(responseCode = "204", description = "Password changed successfully"),
                    @ApiResponse(responseCode = "400", description = "Invalid input data"),
                    @ApiResponse(responseCode = "401", description = "Invalid current password")
            }
    )
    @PatchMapping("/me/password")
    public ResponseEntity<Void> changePassword(
            @Parameter(description = "Unique user identifier", hidden = true)
            @RequestHeader(USER_ID) @NotBlank String id,

            @Parameter(description = "Current and new password", required = true)
            @RequestBody @Valid ChangePasswordRequest changePasswordRequest,

            @Parameter(description = "Refresh token from cookie", required = true)
            @CookieValue(name = "refresh_token") @NotBlank String refreshToken,

            HttpServletResponse response) {
        userService.changePassword(id, changePasswordRequest.getOldPassword(), changePasswordRequest.getNewPassword());
        revokeTokenAndClearCookie(refreshToken, response);
        return ResponseEntity.noContent().build();
    }

    /**
     * Deletes user account and logs out from all devices.
     *
     * @param id           User ID passed in the request header (required, non-empty)
     * @param refreshToken Refresh token from cookie
     * @param response     {@link HttpServletResponse} for clearing cookies
     * @return {@link ResponseEntity} with status code 204 (No Content)
     * @throws jakarta.validation.ConstraintViolationException if user ID is invalid
     */
    @Operation(
            summary = "Delete account",
            description = "Deletes user account and logs out from all devices",
            responses = {
                    @ApiResponse(responseCode = "204", description = "Account deleted successfully"),
                    @ApiResponse(responseCode = "400", description = "Invalid user ID")
            }
    )
    @DeleteMapping("/me")
    public ResponseEntity<Void> deleteCurrentUser(
            @Parameter(description = "Unique user identifier", hidden = true)
            @RequestHeader(USER_ID) @NotBlank String id,

            @Parameter(description = "Refresh token from cookie")
            @CookieValue(name = "refresh_token") String refreshToken,

            HttpServletResponse response) {
        userService.deleteById(UUID.fromString(id));
        revokeTokenAndClearCookie(refreshToken, response);
        return ResponseEntity.noContent().build();
    }

    /**
     * Revokes the refresh token and clears the cookie.
     *
     * @param refreshToken Refresh token to revoke
     * @param response     {@link HttpServletResponse} for clearing cookies
     */
    private void revokeTokenAndClearCookie(String refreshToken, HttpServletResponse response) {
        refreshTokenService.revokeAllUserTokens(refreshToken);
        response.addHeader(HttpHeaders.SET_COOKIE,
                CookieUtils.createTokenCookie(StringUtils.EMPTY, Duration.ZERO).toString());
    }
}