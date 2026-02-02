package ru.zeker.common.dto.task.json;

import com.fasterxml.jackson.annotation.JsonView;
import jakarta.validation.constraints.NotEmpty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public final class CodingTaskContent implements TaskContent {

    @JsonView(Views.Public.class)
    private String templateCode;

    @NotEmpty(message = "The testCases cannot be empty")
    @JsonView(Views.Public.class)
    private List<TestCase> testCases;
}
