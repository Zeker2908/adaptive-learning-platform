package ru.zeker.solution.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import ru.zeker.common.dto.task.response.TaskResponse;
import ru.zeker.solution.client.TaskClient;

import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import static ru.zeker.solution.constant.Confidences.DEFAULT_CONFIDENCE;
import static ru.zeker.solution.constant.Confidences.DIFFICULTY_WEIGHT_SUM;
import static ru.zeker.solution.constant.Confidences.MAX_CONFIDENCE;

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
            // Нет прогресса → даём случайные задачи
            candidateTasks = taskClient.getRandomTasks(CANDIDATE_TASKS_LIMIT);
        } else {
            // Получаем задачи по слабым темам
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
            // Сортируем по убыванию приоритета: самые важные — первые
            return Double.compare(priorityB, priorityA);
        };
    }

    private double calculatePriority(TaskResponse task, Map<String, Double> confidenceMap) {
        // Средний рейтинг по всем тегам задачи (по умолчанию 0.5)
        double avgConfidence = task.getTags().stream()
                .mapToDouble(tag -> confidenceMap.getOrDefault(tag, DEFAULT_CONFIDENCE))
                .average()
                .orElse(DEFAULT_CONFIDENCE);

        // Вес сложности: лёгкие задачи имеют больший приоритет в слабых темах
        double difficultyWeight = DIFFICULTY_WEIGHT_SUM - task.getDifficulty().getRating(); // EASY=0.8 → вес=1.2

        // Приоритет = (1 - уверенность) * вес сложности
        return (MAX_CONFIDENCE - avgConfidence) * difficultyWeight;
    }
}
