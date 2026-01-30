package ru.zeker.task.domain.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import ru.zeker.common.dto.task.request.TaskRequest;
import ru.zeker.common.dto.task.response.TaskResponse;
import ru.zeker.task.domain.model.entity.Tag;
import ru.zeker.task.domain.model.entity.Task;

import java.util.Objects;
import java.util.Set;

@Mapper(componentModel = "spring", imports = {Tag.class, java.util.stream.Collectors.class})
public interface TaskMapper {

    @Mapping(target = "tags", expression = "java(task.getTags().stream().map(Tag::getName).collect(Collectors.toSet()))")
    TaskResponse toResponse(Task task);

    default Task toEntity(TaskRequest request, Set<Tag> tagEntities) {
        if (Objects.isNull(request)) {
            return null;
        }

        return Task.builder()
                .title(request.getTitle())
                .description(request.getDescription())
                .difficulty(request.getDifficulty())
                .tags(tagEntities)
                .templateCode(request.getTemplateCode())
                .tests(request.getTests())
                .build();
    }
}