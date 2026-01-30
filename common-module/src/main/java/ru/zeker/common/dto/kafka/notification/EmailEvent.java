package ru.zeker.common.dto.kafka.notification;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Map;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
@JsonIgnoreProperties(ignoreUnknown = true)
public class EmailEvent {
    @NotNull
    private EmailEventType type;

    @NotBlank
    private String id;

    @NotBlank
    @Email
    private String email;

    @NotNull
    private Map<String, String> payload;

}
