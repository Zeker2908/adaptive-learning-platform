package ru.zeker.common.dto.solution.request;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import ru.zeker.common.dto.solution.Language;

import java.util.UUID;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
@JsonIgnoreProperties(ignoreUnknown = true)
public class SolutionRequest {

    @NotNull(message = "The taskId cannot be null")
    private UUID taskId;

    @NotBlank(message = "The answer cannot be empty")
    @Size(min = 1, max = 10000, message = "Answer must be between 1 and 10,000 characters")
    private String answer;

    private Language language;

}
