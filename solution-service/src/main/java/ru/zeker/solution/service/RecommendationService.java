package ru.zeker.solution.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import ru.zeker.common.dto.task.response.TaskResponse;
import ru.zeker.solution.service.client.TaskClient;

import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import static ru.zeker.solution.domain.constant.Confidences.DEFAULT_CONFIDENCE;
import static ru.zeker.solution.domain.constant.Confidences.DIFFICULTY_WEIGHT_SUM;
import static ru.zeker.solution.domain.constant.Confidences.MAX_CONFIDENCE;

@Service
@RequiredArgsConstructor
public class RecommendationService {

    private static final int CANDIDATE_TASKS_LIMIT = 30;
    private static final int WEAK_TOPICS_LIMIT = 3;

    private final UserProgressService userProgressService;
    private final TaskClient taskClient;

    public List<TaskResponse> getRecommendedTasks(UUID userId, int limit) {
        List<String> weakTopics = userProgressService.getWeakestTopics(userId, WEAK_TOPICS_LIMIT);

        List<TaskResponse> candidateTasks;
        if (weakTopics.isEmpty()) {
            //  No progress → we give random tasks
            candidateTasks = taskClient.getRandomTasks(CANDIDATE_TASKS_LIMIT);
        } else {
            // We receive tasks on weak topics
            candidateTasks = taskClient.getTasksByTags(weakTopics, CANDIDATE_TASKS_LIMIT);
        }

        if (candidateTasks.isEmpty()) {
            return Collections.emptyList();
        }

        Map<String, Double> confidenceMap = userProgressService.getUserConfidenceMap(userId);
        return candidateTasks.stream()
                .sorted(comparatorByAdaptivePriority(confidenceMap))
                .limit(limit)
                .toList();
    }

    private Comparator<TaskResponse> comparatorByAdaptivePriority(Map<String, Double> confidenceMap) {
        return (a, b) -> {
            double priorityA = calculatePriority(a, confidenceMap);
            double priorityB = calculatePriority(b, confidenceMap);
            // Sort by descending priority: most important first
            return Double.compare(priorityB, priorityA);
        };
    }

    private double calculatePriority(TaskResponse task, Map<String, Double> confidenceMap) {
        // Average rating for all task tags (default 0.5)
        double avgConfidence = task.getTags().stream()
                .mapToDouble(tag -> confidenceMap.getOrDefault(tag, DEFAULT_CONFIDENCE))
                .average()
                .orElse(DEFAULT_CONFIDENCE);

        // Difficulty weighting: easy problems have higher priority in weak topics
        double difficultyWeight = DIFFICULTY_WEIGHT_SUM - task.getDifficulty().getRating(); // EASY=0.8 → вес=1.2

        // Priority = (1 - Confidence) * Difficulty Weight
        return (MAX_CONFIDENCE - avgConfidence) * difficultyWeight;
    }
}
