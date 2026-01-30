package ru.zeker.common.dto.kafka.solution;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import ru.zeker.common.dto.solution.Language;
import ru.zeker.common.dto.task.TestCase;

import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
@JsonIgnoreProperties(ignoreUnknown = true)
public class SolutionExecRequest {

    private String solutionId;
    private Language language;
    private String code;
    private List<TestCase> tests;
}
