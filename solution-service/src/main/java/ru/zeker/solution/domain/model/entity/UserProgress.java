package ru.zeker.solution.domain.model.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Index;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.proxy.HibernateProxy;
import ru.zeker.common.model.BaseEntity;

import java.util.Objects;
import java.util.UUID;

import static ru.zeker.solution.constant.Confidences.MAX_CONFIDENCE_STR;
import static ru.zeker.solution.constant.Confidences.MIN_CONFIDENCE_STR;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "user_progress", uniqueConstraints = {
        @UniqueConstraint(columnNames = {"userId", "topic"})
},
        indexes = {
                @Index(columnList = "userId"),
                @Index(columnList = "userId, confidence")
        })
public class UserProgress extends BaseEntity {

    @Column(nullable = false)
    private String topic;

    @Column(nullable = false)
    private UUID userId;

    // 0.0 - 1.0
    @Column(nullable = false)
    @DecimalMin(value = MIN_CONFIDENCE_STR)
    @DecimalMax(value = MAX_CONFIDENCE_STR)
    private double confidence;

    @Override
    public final boolean equals(Object o) {
        if (this == o) return true;
        if (o == null) return false;
        Class<?> oEffectiveClass = o instanceof HibernateProxy proxy ? proxy.getHibernateLazyInitializer().getPersistentClass() : o.getClass();
        Class<?> thisEffectiveClass = this instanceof HibernateProxy proxy ? proxy.getHibernateLazyInitializer().getPersistentClass() : this.getClass();
        if (thisEffectiveClass != oEffectiveClass) return false;
        UserProgress task = (UserProgress) o;
        return getId() != null && Objects.equals(getId(), task.getId());
    }

    @Override
    public final int hashCode() {
        return this instanceof HibernateProxy proxy ? proxy.getHibernateLazyInitializer().getPersistentClass().hashCode() : getClass().hashCode();
    }
}