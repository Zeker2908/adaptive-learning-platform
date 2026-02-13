package ru.zeker.solution.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import ru.zeker.common.dto.solution.response.TaskStatisticsResponse;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class SolutionStatisticsFacade {

    private final SolutionService solutionService;
    private final UserProgressService userProgressService;

    public TaskStatisticsResponse buildStatistics(UUID userId) {
        long total = solutionService.countUserSolutions(userId);
        long success = solutionService.countUserSuccessfulSolutions(userId);

        double successRate = total == 0
                ? 0
                : (double) success / total * 100;

        double avgConfidence = userProgressService.getAverageConfidence(userId);
        var weakestTopics = userProgressService.getWeakestTopics(userId, 5);

        return TaskStatisticsResponse.builder()
                .totalSolutions(total)
                .successfulSolutions(success)
                .successRate(round(successRate))
                .averageConfidence(round(avgConfidence))
                .weakestTopics(weakestTopics)
                .build();
    }

    private double round(double value) {
        return Math.round(value * 100.0) / 100.0;
    }
}