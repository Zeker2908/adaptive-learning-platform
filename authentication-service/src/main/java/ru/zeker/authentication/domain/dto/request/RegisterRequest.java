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
@Schema(description = "Запрос на регистрацию нового пользователя")
public class RegisterRequest {

    @Schema(description = "Email пользователя", example = "user@example.com", required = true)
    @NotBlank(message = "Адрес электронной почты не может быть пустым")
    @Email(message = "Адрес электронной почты должен быть в формате user@example.com")
    @Size(min = 5, max = 255, message = "Адрес электронной почты должен содержать от 5 до 255 символов")
    private String email;

    @Schema(
            description = "Пароль пользователя. Должен содержать минимум 8 символов, включая хотя бы одну букву и одну цифру",
            example = "myPass123"
    )
    @Pattern(
            regexp = "^(?=.*[A-Za-z])(?=.*\\d).{8,255}$",
            message = "Пароль должен содержать минимум 8 символов, включая хотя бы одну букву и одну цифру"
    )
    @NotBlank(message = "Пароль не может быть пустым")
    @Size(min = 8, max = 255, message = "Длина пароля должна быть от 8 до 255 символов")
    private String password;
}