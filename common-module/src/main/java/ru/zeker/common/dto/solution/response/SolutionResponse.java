package ru.zeker.common.dto.solution.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import ru.zeker.common.dto.solution.Language;

import java.util.UUID;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class SolutionResponse {

    private UUID id;
    private UUID userId;
    private UUID taskId;
    private String code;
    private Language language;
    private String status;
    private String feedback;
}
