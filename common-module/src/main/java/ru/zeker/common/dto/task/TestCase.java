package ru.zeker.common.dto.task;


import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TestCase {

    @NotBlank
    private String input;

    @NotBlank
    private String output;
}