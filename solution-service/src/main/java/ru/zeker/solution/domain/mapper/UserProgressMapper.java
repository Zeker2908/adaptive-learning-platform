package ru.zeker.solution.domain.mapper;

import org.mapstruct.Mapper;
import ru.zeker.common.dto.solution.response.UserProgressResponse;
import ru.zeker.solution.domain.model.entity.UserProgress;

@Mapper(componentModel = "spring")
public interface UserProgressMapper {

    UserProgressResponse toResponse(UserProgress userProgress);
}
