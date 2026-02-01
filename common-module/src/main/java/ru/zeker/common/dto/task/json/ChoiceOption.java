package ru.zeker.common.dto.task.json;

import com.fasterxml.jackson.annotation.JsonView;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ChoiceOption {
    @NotBlank
    @JsonView(Views.Public.class)
    private String text;

    @JsonView(Views.Public.class)
    private String explanation;
}