package ru.zeker.common.dto.task.response;

import com.fasterxml.jackson.annotation.JsonView;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import ru.zeker.common.dto.task.Difficulty;
import ru.zeker.common.dto.task.TestCase;
import ru.zeker.common.dto.task.json.TaskContent;
import ru.zeker.common.dto.task.json.Views;

import java.util.List;
import java.util.Set;
import java.util.UUID;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class TaskResponse {

    @JsonView(Views.Public.class)
    private UUID id;
    @JsonView(Views.Public.class)
    private String title;
    @JsonView(Views.Public.class)
    private String description;
    @JsonView(Views.Public.class)
    private Difficulty difficulty;
    @JsonView(Views.Public.class)
    private Set<String> tags;
    @JsonView(Views.Public.class)
    private TaskContent content;
}
