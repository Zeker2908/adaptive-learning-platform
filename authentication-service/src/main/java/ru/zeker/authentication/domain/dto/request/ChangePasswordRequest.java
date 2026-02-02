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
@Schema(description = "Password change request")
public class ChangePasswordRequest {

    @NotBlank
    @Schema(description = "Current Password", example = "OPass232!")
    private String oldPassword;

    @NotBlank(message = "The password cannot be empty")
    @Size(min = 8, max = 255, message = "The password length must be between 8 and 255 characters.")
    @Pattern(
            regexp = "^(?=.*[A-Za-z])(?=.*\\d).{8,255}$",
            message = "The password must contain at least 8 characters, including at least one letter and one number."
    )
    @Schema(
            description = "New password. Must contain an uppercase letter, a lowercase letter, a number, and a special character.",
            example = "NewPass228!",
            minLength = 8,
            maxLength = 255
    )
    private String newPassword;
}