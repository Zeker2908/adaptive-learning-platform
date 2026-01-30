package ru.zeker.common.dto.solution.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class UserProgressResponse {

    private UUID id;
    private String topic;
    private double confidence;
}
