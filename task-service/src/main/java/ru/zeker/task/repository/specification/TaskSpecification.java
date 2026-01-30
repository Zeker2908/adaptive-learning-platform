package ru.zeker.task.repository.specification;

import lombok.experimental.UtilityClass;
import org.springframework.data.jpa.domain.Specification;
import ru.zeker.common.dto.task.Difficulty;
import ru.zeker.task.domain.model.entity.Task;

import java.util.List;

@UtilityClass
public class TaskSpecification {

    public static Specification<Task> hasTitle(String title) {
        return (root, query, builder) ->
                (title == null || title.isBlank())
                        ? builder.conjunction()
                        : builder.like(builder.lower(root.get("title")), "%" + title.toLowerCase() + "%");
    }

    public static Specification<Task> hasDifficulties(List<Difficulty> diffs) {
        return (root, query, builder) -> {
            if (diffs == null || diffs.isEmpty()) {
                return builder.conjunction();
            }
            return root.get("difficulty").in(diffs);
        };
    }


    public static Specification<Task> hasAnyTags(List<String> tagNames) {
        return (root, query, builder) -> {
            if (tagNames == null || tagNames.isEmpty()) {
                return builder.conjunction();
            }
            query.distinct(true);
            var join = root.join("tags");
            return join.get("name").in(tagNames);
        };
    }

}