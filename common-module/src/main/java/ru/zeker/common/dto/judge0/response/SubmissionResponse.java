package ru.zeker.common.dto.judge0.response;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class SubmissionResponse {

    private String stdout;
    private String stderr;
    @JsonProperty("compile_output")
    private String compileOutput;
    private Status status;
    private String message;
    private Float time;
    private Float memory;
}
