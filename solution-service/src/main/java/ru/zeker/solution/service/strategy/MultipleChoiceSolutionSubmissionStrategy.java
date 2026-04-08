package ru.zeker.solution.service.strategy;

import lombok.RequiredArgsConstructor;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Component;
import ru.zeker.common.dto.solution.SolutionStatus;
import ru.zeker.common.dto.solution.request.SolutionRequest;
import ru.zeker.common.dto.task.json.MultipleChoiceTaskContent;
import ru.zeker.common.dto.task.json.TaskContent;
import ru.zeker.solution.domain.model.entity.Solution;
import ru.zeker.solution.repository.SolutionRepository;

import java.util.Arrays;
import java.util.UUID;
import java.util.stream.Collectors;

@Component
@RequiredArgsConstructor
public class MultipleChoiceSolutionSubmissionStrategy implements SolutionSubmissionStrategy<MultipleChoiceTaskContent> {

    private final SolutionRepository solutionRepository;

    @Override
    public boolean support(TaskContent content) {
        return content instanceof MultipleChoiceTaskContent;
    }

    @Override
    public Solution handle(SolutionRequest request, UUID userId, MultipleChoiceTaskContent taskContent) {
        var status = isCorrectAnswer(request.getAnswer(), taskContent)
                ? SolutionStatus.SUCCESS
                : SolutionStatus.FAILED;

        var solution = Solution.builder()
                .userId(userId)
                .taskId(request.getTaskId())
                .answer(request.getAnswer())
                .status(status)
                .build();

        return solutionRepository.save(solution);
    }

    private boolean isCorrectAnswer(String answer, MultipleChoiceTaskContent taskContent) {
        var parsedAnswer = Arrays.stream(
                        answer.replaceAll("[\\[\\]\\s]", StringUtils.EMPTY)
                                .split(",")
                )
                .filter(StringUtils::isNotEmpty)
                .filter(str -> str.matches("-?\\d+"))
                .map(Integer::parseInt)
                .collect(Collectors.toSet());

        return taskContent.getCorrectOptionIndices().equals(parsedAnswer);
    }
}
