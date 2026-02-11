package ru.zeker.authentication.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import ru.zeker.authentication.domain.model.entity.PasswordHistory;

import java.util.Set;
import java.util.UUID;


@Repository
public interface PasswordHistoryRepository extends JpaRepository<PasswordHistory, UUID> {
    Set<PasswordHistory> findAllByLocalAuthId(UUID localAuthId);

    @Modifying
    @Query(
            value = "DELETE FROM password_history " +
                    "WHERE id IN (" +
                    "  SELECT id FROM password_history " +
                    "  WHERE local_auth_id = :localAuthId " +
                    "  ORDER BY created_at ASC " +
                    "  LIMIT :count" +
                    ")",
            nativeQuery = true
    )
    void deleteOldestByLocalAuthId(@Param("localAuthId") UUID localAuthId,
                                   @Param("count") int count);
}
