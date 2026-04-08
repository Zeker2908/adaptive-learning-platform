package ru.zeker.task.repository.specification;

import lombok.experimental.UtilityClass;
import org.springframework.data.jpa.domain.Specification;
import ru.zeker.common.dto.task.Difficulty;
import ru.zeker.task.domain.model.entity.Task;

import java.util.List;
import java.util.Objects;

@UtilityClass
public class TaskSpecification {

    public static Specification<Task> hasTitle(String title) {
        return (root, query, builder) ->
                (Objects.isNull(title) || title.isBlank())
                        ? builder.conjunction()
                        : builder.like(builder.lower(root.get("title")), "%" + title.toLowerCase() + "%");
    }

    public static Specification<Task> hasDifficulties(List<Difficulty> diffs) {
        return (root, query, builder) -> {
            if (Objects.isNull(diffs) || diffs.isEmpty()) {
                return builder.conjunction();
            }
            return root.get("difficulty").in(diffs);
        };
    }

    public static Specification<Task> hasAllTags(List<String> tagNames) {
        return (root, query, builder) -> {
            if (Objects.isNull(tagNames) || tagNames.isEmpty()) {
                return builder.conjunction();
            }

            var join = root.join("tags");

            query.groupBy(root.get("id"));
            query.having(
                    builder.equal(
                            builder.countDistinct(join.get("name")),
                            tagNames.size()
                    )
            );

            return join.get("name").in(tagNames);
        };
    }

    public static Specification<Task> hasAnyTags(List<String> tagNames) {
        return (root, query, builder) -> {
            if (Objects.isNull(tagNames) || tagNames.isEmpty()) {
                return builder.conjunction();
            }
            query.distinct(true);
            var join = root.join("tags");
            return join.get("name").in(tagNames);
        };
    }

}