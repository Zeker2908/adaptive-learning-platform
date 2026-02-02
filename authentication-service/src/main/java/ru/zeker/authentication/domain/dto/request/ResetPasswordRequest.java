package ru.zeker.authentication.domain.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Schema(description = "Password reset request")
public class ResetPasswordRequest {

    @Schema(description = "Password reset token", example = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...", required = true)
    @NotBlank
    private String token;

    @Schema(description = "New password", example = "N3wP@ssw0rd!", required = true)
    @Pattern(
            regexp = "^(?=.*[A-Za-z])(?=.*\\d).{8,255}$",
            message = "Password must contain at least 8 characters, including at least one letter and one digit"
    )
    @NotBlank(message = "Password cannot be empty")
    @Size(min = 8, max = 255, message = "Password length must be between 8 and 255 characters")
    private String password;
}