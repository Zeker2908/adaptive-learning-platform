package ru.zeker.common.dto.task.json;

import com.fasterxml.jackson.annotation.JsonView;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public final class SingleChoiceTaskContent implements TaskContent {

    @NotBlank(message = "The question cannot be empty")
    @JsonView(Views.Public.class)
    private String question;

    @NotEmpty(message = "The options cannot be empty")
    @JsonView(Views.Public.class)
    private List<ChoiceOption> options;

    @NotNull(message = "The correctOptionIndex cannot be null")
    @JsonView(Views.Admin.class)
    private int correctOptionIndex;
}
