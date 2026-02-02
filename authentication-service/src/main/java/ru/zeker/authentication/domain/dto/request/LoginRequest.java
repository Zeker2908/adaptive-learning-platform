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
@Schema(description = "User authentication request")
public class LoginRequest {

    @Schema(description = "User email", example = "user@example.com", required = true)
    @NotBlank(message = "Email address cannot be empty")
    @Email(message = "Email address must be in the format user@example.com")
    @Size(min = 5, max = 255, message = "Email address must be between 5 and 255 characters")
    private String email;

    @Schema(description = "User password", example = "P@ssw0rd123", required = true)
    @NotBlank(message = "Password cannot be empty")
    @Size(max = 255, message = "Password length must be up to 255 characters")
    private String password;
}