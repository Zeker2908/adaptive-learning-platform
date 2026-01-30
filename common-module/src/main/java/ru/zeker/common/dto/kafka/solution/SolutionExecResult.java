package ru.zeker.common.dto.kafka.solution;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import ru.zeker.common.dto.solution.SolutionStatus;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
@JsonIgnoreProperties(ignoreUnknown = true)
public class SolutionExecResult {

    private String solutionId;
    private SolutionStatus status;
    private String descriptionError;
}
