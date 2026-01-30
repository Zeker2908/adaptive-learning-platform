package ru.zeker.solution.repository;

import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import ru.zeker.solution.domain.model.entity.UserProgress;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface UserProgressRepository extends JpaRepository<UserProgress, UUID> {
    List<UserProgress> findByUserId(UUID userId);

    Optional<UserProgress> findByUserIdAndTopic(UUID userId, String topic);

    @Query("SELECT up FROM UserProgress up WHERE up.userId = :userId ORDER BY up.confidence ASC")
    List<UserProgress> findWeakestTopicsByUserId(@Param("userId") UUID userId, Pageable pageable);
}
