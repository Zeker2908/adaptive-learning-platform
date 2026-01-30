package ru.zeker.common.dto.kafka.solution;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
@JsonIgnoreProperties(ignoreUnknown = true)
public class TestResult {

    private String input;
    private String expected;
    private String actual;
    private boolean passed;
    private int timeMs;
}
