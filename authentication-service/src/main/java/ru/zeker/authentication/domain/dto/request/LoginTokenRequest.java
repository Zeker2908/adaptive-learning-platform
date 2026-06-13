package ru.zeker.authentication.domain.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Schema(description = "Request for demo login by one-time token")
public class LoginTokenRequest {

    @Schema(description = "Demo login token from QR link", required = true)
    @NotBlank
    private String token;
}
