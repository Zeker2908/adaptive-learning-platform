package ru.zeker.common.dto.task.request;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import ru.zeker.common.dto.task.Difficulty;
import ru.zeker.common.dto.task.json.TaskContent;

import java.util.Set;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
@JsonIgnoreProperties(ignoreUnknown = true)
public class TaskRequest {

    @NotBlank(message = "The title cannot be empty")
    @Size(min = 8, max = 255, message = "The title length must be between 8 and 255 characters.")
    private String title;

    @Size(max = 1000, message = "The description length must be between 1000 characters.")
    private String description;

    @NotNull(message = "The difficulty cannot be null")
    private Difficulty difficulty;

    @NotEmpty(message = "The tags cannot be empty")
    private Set<String> tags;

    @NotNull(message = "The content cannot be null")
    @Valid
    private TaskContent content;
}
