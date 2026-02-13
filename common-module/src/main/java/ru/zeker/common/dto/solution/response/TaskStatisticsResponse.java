package ru.zeker.common.dto.solution.response;


import lombok.Builder;

import java.util.List;

@Builder
public record TaskStatisticsResponse(

        long totalSolutions,
        long successfulSolutions,
        double successRate,
        double averageConfidence,
        List<String> weakestTopics
) {}