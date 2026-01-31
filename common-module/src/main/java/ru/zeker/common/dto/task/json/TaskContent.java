package ru.zeker.common.dto.task.json;

import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import ru.zeker.common.dto.task.consts.TaskType;

@JsonTypeInfo(use = JsonTypeInfo.Id.NAME, property = "type")
@JsonSubTypes({
        @JsonSubTypes.Type(value = CodingTaskContent.class, name = TaskType.CODING),
        @JsonSubTypes.Type(value = MultipleChoiceTaskContent.class, name = TaskType.MULTIPLE_CHOICE),
        @JsonSubTypes.Type(value = SingleChoiceTaskContent.class, name = TaskType.SINGLE_CHOICE)
})
public sealed interface TaskContent permits CodingTaskContent, MultipleChoiceTaskContent, SingleChoiceTaskContent {
}