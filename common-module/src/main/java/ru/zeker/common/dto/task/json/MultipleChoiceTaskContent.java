package ru.zeker.common.dto.task.json;

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

    @NotBlank
    private String question;
    @NotEmpty
    private List<ChoiceOption> options;
    @NotEmpty
    private Set<Integer> correctOptionIndices;
}
