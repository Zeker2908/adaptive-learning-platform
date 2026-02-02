package ru.zeker.authentication.domain.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class RefreshTokenRequest {
    @Size(max = 512, message = "The refresh token must be 512 characters long")
    @NotBlank(message = "The refresh token cannot be empty")
    private String refreshToken;
}
