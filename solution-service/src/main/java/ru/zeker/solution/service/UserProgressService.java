package ru.zeker.solution.service;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import ru.zeker.solution.domain.model.entity.UserProgress;
import ru.zeker.solution.repository.UserProgressRepository;

import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

import static ru.zeker.solution.domain.constant.Confidences.DEFAULT_CONFIDENCE;
import static ru.zeker.solution.domain.constant.Confidences.DIFFICULTY_WEIGHT_SUM;
import static ru.zeker.solution.domain.constant.Confidences.MAX_CONFIDENCE;
import static ru.zeker.solution.domain.constant.Confidences.MIN_CONFIDENCE;

@Slf4j
@Service
@RequiredArgsConstructor
public class UserProgressService {

    private static final double SUCCESS_FACTOR = 0.1;
    private static final double FAILURE_BASE = 0.05;

    private final UserProgressRepository repository;

    public List<UserProgress> getUserProgress(UUID userId) {
        return repository.findByUserId(userId);
    }

    public List<String> getWeakestTopics(UUID userId, int maxTopics) {
        var weakest = repository.findWeakestTopicsByUserId(
                userId,
                PageRequest.of(0, maxTopics)
        );
        return weakest.stream()
                .map(UserProgress::getTopic)
                .toList();
    }

    public Map<String, Double> getUserConfidenceMap(UUID userId) {
        return repository.findByUserId(userId).stream()
                .collect(Collectors.toMap(UserProgress::getTopic, UserProgress::getConfidence));
    }

    @Transactional
    public void updateOrCreate(String topic, UUID userId, double difficulty, boolean success, int totalTags) {
        var progress = repository
                .findByUserIdAndTopic(userId, topic)
                .orElseGet(() -> UserProgress.builder()
                        .userId(userId)
                        .topic(topic)
                        .confidence(DEFAULT_CONFIDENCE)
                        .build()
                );

        var oldConfidence = progress.getConfidence();
        var delta = (success
                ? calculateSuccessDelta(oldConfidence, difficulty)
                : calculateFailureDelta(difficulty))
                / Math.max(1, totalTags);

        var newConfidence = calculateNewConfidence(oldConfidence, delta);
        progress.setConfidence(newConfidence);
        repository.save(progress);
    }

    private double calculateNewConfidence(double oldConfidence, double delta) {
        return Math.min(MAX_CONFIDENCE, Math.max(MIN_CONFIDENCE, oldConfidence + delta));
    }

    private double calculateSuccessDelta(double oldConfidence, double difficulty) {
        return (MAX_CONFIDENCE - oldConfidence) * SUCCESS_FACTOR * difficulty;
    }

    private double calculateFailureDelta(double difficulty) {
        return -FAILURE_BASE * (DIFFICULTY_WEIGHT_SUM - difficulty);
    }
}
