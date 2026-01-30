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

import static ru.zeker.solution.constant.Confidences.DEFAULT_CONFIDENCE;
import static ru.zeker.solution.constant.Confidences.DIFFICULTY_WEIGHT_SUM;
import static ru.zeker.solution.constant.Confidences.MAX_CONFIDENCE;
import static ru.zeker.solution.constant.Confidences.MIN_CONFIDENCE;

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
        List<UserProgress> weakest = repository.findWeakestTopicsByUserId(
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
        UserProgress progress = repository
                .findByUserIdAndTopic(userId, topic)
                .orElseGet(() -> UserProgress.builder()
                        .userId(userId)
                        .topic(topic)
                        .confidence(DEFAULT_CONFIDENCE)
                        .build()
                );

        double oldConfidence = progress.getConfidence();

        double baseDelta = success
                ? (MAX_CONFIDENCE - oldConfidence) * SUCCESS_FACTOR * difficulty
                : -FAILURE_BASE * (DIFFICULTY_WEIGHT_SUM - difficulty);

        double delta = baseDelta / Math.max(1, totalTags);

        double newConfidence = Math.min(MAX_CONFIDENCE, Math.max(MIN_CONFIDENCE, oldConfidence + delta));
        progress.setConfidence(newConfidence);
        repository.save(progress);
    }
}
