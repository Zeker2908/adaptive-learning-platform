package ru.zeker.solution.service.strategy;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import ru.zeker.common.dto.solution.SolutionStatus;
import ru.zeker.common.dto.solution.request.SolutionRequest;
import ru.zeker.common.dto.task.json.SingleChoiceTaskContent;
import ru.zeker.common.dto.task.json.TaskContent;
import ru.zeker.solution.domain.model.entity.Solution;
import ru.zeker.solution.repository.SolutionRepository;

import java.util.UUID;

@Component
@RequiredArgsConstructor
public class SingleChoiceSolutionSubmissionStrategy implements SolutionSubmissionStrategy<SingleChoiceTaskContent> {

    private final SolutionRepository solutionRepository;

    @Override
    public boolean support(TaskContent content) {
        return content instanceof SingleChoiceTaskContent;
    }

    @Override
    public Solution handle(SolutionRequest request, UUID userId, SingleChoiceTaskContent taskContent) {
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

    private boolean isCorrectAnswer(String answer, SingleChoiceTaskContent taskContent) {
        var trimmed = answer.trim();
        if (!trimmed.matches("-?\\d+")) {
            return false;
        }
        try {
            return taskContent.getCorrectOptionIndex() == Integer.parseInt(trimmed);
        } catch (NumberFormatException e) {
            return false;
        }
    }
}
