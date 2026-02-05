package ru.zeker.authentication.domain.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
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
@Schema(description = "New user registration request")
public class RegisterRequest {

    @Schema(description = "User email", example = "user@example.com", required = true)
    @NotBlank(message = "Email address cannot be empty")
    @Email(message = "Email address must be in the format user@example.com")
    @Size(min = 5, max = 255, message = "Email address must contain between 5 and 255 characters")
    private String email;

    @Schema(
            description = "User password. Must contain at least 8 characters, including at least one letter and one digit",
            example = "myPass123"
    )
    @Pattern(
            regexp = "^(?=.*[A-Za-z])(?=.*\\d).{8,255}$",
            message = "Password must contain at least 8 characters, including at least one letter and one digit"
    )
    @NotBlank(message = "Password cannot be empty")
    @Size(min = 8, max = 255, message = "Password length must be between 8 and 255 characters")
    private String password;

    @Schema(description = "User first name", example = "John", required = true)
    @NotBlank(message = "First name cannot be empty")
    @Size(min = 1, max = 100, message = "First name must contain between 1 and 100 characters")
    private String firstName;

    @Schema(description = "User last name", example = "Doe")
    @Size(max = 100, message = "Last name must not exceed 100 characters")
    private String lastName;
}