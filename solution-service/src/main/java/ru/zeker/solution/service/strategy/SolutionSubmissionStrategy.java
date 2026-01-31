package ru.zeker.solution.service.strategy;

import ru.zeker.common.dto.solution.request.SolutionRequest;
import ru.zeker.common.dto.task.json.TaskContent;
import ru.zeker.solution.domain.model.entity.Solution;

public interface SolutionSubmissionStrategy<T extends TaskContent> {

    boolean support(TaskContent content);

    Solution handle(SolutionRequest request, String userId, T taskContent);
}
