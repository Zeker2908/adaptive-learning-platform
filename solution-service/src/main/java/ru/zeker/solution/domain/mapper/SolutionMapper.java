package ru.zeker.solution.domain.mapper;

import org.mapstruct.AfterMapping;
import org.mapstruct.Context;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import org.mapstruct.Named;
import ru.zeker.common.dto.kafka.solution.SolutionExecRequest;
import ru.zeker.common.dto.solution.response.SolutionResponse;
import ru.zeker.common.dto.task.TestCase;
import ru.zeker.solution.domain.model.entity.Solution;
import ru.zeker.common.dto.solution.SolutionStatus;

import java.util.List;
import java.util.UUID;

@Mapper(componentModel = "spring")
public interface SolutionMapper {

    @Mapping(target = "solutionId", source = "id")
    SolutionExecRequest toKafkaMessage(Solution solution, @Context List<TestCase> tests);

    @Mapping(target = "status", source = "status", qualifiedByName = "solutionStatusToString")
    SolutionResponse toResponse(Solution solution);

    @Named("solutionStatusToString")
    default String solutionStatusToString(SolutionStatus status) {
        return status != null ? status.name() : null;
    }

    @Named("uuidToString")
    default String uuidToString(UUID uuid) {
        return uuid != null ? uuid.toString() : null;
    }

    @AfterMapping
    default void addTests(Solution solution, @MappingTarget SolutionExecRequest request, @Context List<TestCase> tests) {
        request.setTests(tests);
    }
}
