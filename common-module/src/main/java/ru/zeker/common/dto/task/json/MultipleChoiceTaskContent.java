package ru.zeker.common.dto.task.json;

import com.fasterxml.jackson.annotation.JsonView;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.Set;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public final class MultipleChoiceTaskContent implements TaskContent {

    @NotBlank(message = "The question cannot be empty")
    @JsonView(Views.Public.class)
    private String question;

    @NotEmpty(message = "The options cannot be empty")
    @JsonView(Views.Public.class)
    private List<ChoiceOption> options;

    @NotEmpty(message = "The correctOptionIndices cannot be empty")
    @JsonView(Views.Admin.class)
    private Set<Integer> correctOptionIndices;
}
