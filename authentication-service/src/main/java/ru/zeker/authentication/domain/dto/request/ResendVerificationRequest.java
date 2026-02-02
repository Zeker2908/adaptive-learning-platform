package ru.zeker.authentication.domain.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Schema(description = "Request to resend the confirmation email")
public class ResendVerificationRequest {

    @Schema(description = "User email", example = "user@example.com")
    @Email
    @NotBlank
    @Size(min = 8, max = 255, message = "Email length must be between 8 and 255 characters.")
    private String email;
}