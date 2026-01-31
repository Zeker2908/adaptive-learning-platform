package ru.zeker.common.dto.task.json;

import jakarta.validation.constraints.NotBlank;
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
public final class SingleChoiceTaskContent implements TaskContent {

    @NotBlank
    private String question;
    @NotEmpty
    private List<ChoiceOption> options;
    private int correctOptionIndex;
}
