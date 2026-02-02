package ru.zeker.authentication.domain.dto.request;

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
public class BindPasswordRequest {
    @Pattern(
            regexp = "^(?=.*[A-Za-z])(?=.*\\d).{8,255}$",
            message = "The password must contain at least 8 characters, including at least one letter and one number."
    )
    @NotBlank(message = "The password cannot be empty")
    @Size(min = 8, max = 255, message = "The password length must be between 8 and 255 characters.")
    private String password;
}
