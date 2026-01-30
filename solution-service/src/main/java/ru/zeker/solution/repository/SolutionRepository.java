package ru.zeker.solution.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import ru.zeker.common.dto.solution.SolutionStatus;
import ru.zeker.solution.domain.model.entity.Solution;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Repository
public interface SolutionRepository extends JpaRepository<Solution, UUID> {

    List<Solution> findByUserId(UUID userId);

    List<Solution>findByStatusAndCreatedAtBefore(SolutionStatus status, LocalDateTime createdAt);

    @Query("SELECT DATE(s.createdAt), COUNT(s) " +
            "FROM Solution s " +
            "WHERE s.userId = :userId AND s.createdAt >= :since " +
            "GROUP BY DATE(s.createdAt) " +
            "ORDER BY DATE(s.createdAt)")
    List<Object[]> findActivityByDay(@Param("userId") UUID userId, @Param("since") LocalDateTime since);
}
