package ru.zeker.solution.service.strategy;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import ru.zeker.common.dto.kafka.solution.SolutionExecRequest;
import ru.zeker.common.dto.solution.SolutionStatus;
import ru.zeker.common.dto.solution.request.SolutionRequest;
import ru.zeker.common.dto.task.json.CodingTaskContent;
import ru.zeker.common.dto.task.json.TaskContent;
import ru.zeker.solution.domain.mapper.SolutionMapper;
import ru.zeker.solution.domain.model.entity.Solution;
import ru.zeker.solution.repository.SolutionRepository;
import ru.zeker.solution.service.KafkaProducer;

import java.util.UUID;

@Component
@RequiredArgsConstructor
public class CodingSolutionSubmissionStrategy implements SolutionSubmissionStrategy<CodingTaskContent> {

    private final SolutionRepository solutionRepository;
    private final KafkaProducer kafkaProducer;
    private final SolutionMapper solutionMapper;

    @Override
    public boolean support(TaskContent content) {
        return content instanceof CodingTaskContent;
    }

    @Override
    @Transactional
    public Solution handle(SolutionRequest request, String userId, CodingTaskContent taskContent) {
        Solution solution = Solution.builder()
                .userId(UUID.fromString(userId))
                .taskId(request.getTaskId())
                .code(request.getCode())
                .language(request.getLanguage())
                .status(SolutionStatus.PENDING)
                .build();
        solution = solutionRepository.save(solution);

        SolutionExecRequest message = solutionMapper.toKafkaMessage(solution, taskContent.getTestCases());
        kafkaProducer.sendExecutionEvent(message);

        return solution;
    }
}
