package ru.zeker.common.dto.task.request;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import ru.zeker.common.dto.task.Difficulty;
import ru.zeker.common.dto.task.TestCase;

import java.util.List;
import java.util.Set;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
@JsonIgnoreProperties(ignoreUnknown = true)
public class TaskRequest {

    @NotBlank
    private String title;

    private String description;

    @NotNull
    private Difficulty difficulty;

    @NotEmpty
    private Set<String> tags;

    private String templateCode;

    @NotEmpty
    private List<TestCase> tests;
}
