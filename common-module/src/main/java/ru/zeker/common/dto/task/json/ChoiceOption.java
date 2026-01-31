package ru.zeker.common.dto.task.json;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ChoiceOption {
    @NotBlank
    private String text;
    private String explanation;
}