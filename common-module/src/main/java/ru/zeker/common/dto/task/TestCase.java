package ru.zeker.common.dto.task;


import com.fasterxml.jackson.annotation.JsonView;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import ru.zeker.common.dto.task.json.Views;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TestCase {

    @NotBlank
    @JsonView(Views.Public.class)
    private String input;

    @NotBlank
    @JsonView(Views.Public.class)
    private String output;
}