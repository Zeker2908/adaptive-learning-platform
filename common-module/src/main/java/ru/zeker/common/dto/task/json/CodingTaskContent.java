package ru.zeker.common.dto.task.json;

import jakarta.validation.constraints.NotEmpty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import ru.zeker.common.dto.task.TestCase;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public final class CodingTaskContent implements TaskContent {

    private String templateCode;
    @NotEmpty
    private List<TestCase> testCases;
}
