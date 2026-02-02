package ru.zeker.solution.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import ru.zeker.common.dto.task.response.TaskResponse;
import ru.zeker.solution.service.client.TaskClient;

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
        var weakTopics = userProgressService.getWeakestTopics(userId, WEAK_TOPICS_LIMIT);

        List<TaskResponse> candidateTasks = weakTopics.isEmpty()
                ? taskClient.getRandomTasks(CANDIDATE_TASKS_LIMIT)
                : taskClient.getTasksByTags(weakTopics, CANDIDATE_TASKS_LIMIT);

        return candidateTasks.stream()
                .sorted(comparatorByAdaptivePriority(userProgressService.getUserConfidenceMap(userId)))
                .limit(limit)
                .toList();
    }

    private Comparator<TaskResponse> comparatorByAdaptivePriority(Map<String, Double> confidenceMap) {
        return Comparator.comparingDouble((TaskResponse task) -> calculatePriority(task, confidenceMap)).reversed();
    }

    private static double calculatePriority(TaskResponse task, Map<String, Double> confidenceMap) {
        // Average rating for all task tags (default 0.5)
        var avgConfidence = task.getTags().stream()
                .mapToDouble(tag -> confidenceMap.getOrDefault(tag, DEFAULT_CONFIDENCE))
                .average()
                .orElse(DEFAULT_CONFIDENCE);

        // Difficulty weighting: easy problems have higher priority in weak topics
        var difficultyWeight = DIFFICULTY_WEIGHT_SUM - task.getDifficulty().getRating(); // EASY=0.8 → HARD=1.2

        // Priority = (1 - Confidence) * Difficulty Weight
        return (MAX_CONFIDENCE - avgConfidence) * difficultyWeight;
    }
}
