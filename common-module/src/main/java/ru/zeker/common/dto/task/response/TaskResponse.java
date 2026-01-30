package ru.zeker.common.dto.task.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import ru.zeker.common.dto.task.Difficulty;
import ru.zeker.common.dto.task.TestCase;

import java.util.List;
import java.util.Set;
import java.util.UUID;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class TaskResponse {

    private UUID id;
    private String title;
    private String description;
    private Difficulty difficulty;
    private Set<String> tags;
    private String templateCode;
    private List<TestCase> tests;
}
