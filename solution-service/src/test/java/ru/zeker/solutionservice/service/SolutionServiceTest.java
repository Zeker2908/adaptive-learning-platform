package ru.zeker.solutionservice.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import ru.zeker.common.dto.kafka.solution.SolutionExecResult;
import ru.zeker.common.dto.solution.SolutionStatus;
import ru.zeker.common.dto.solution.request.SolutionRequest;
import ru.zeker.common.dto.task.Difficulty;
import ru.zeker.common.dto.task.json.CodingTaskContent;
import ru.zeker.common.dto.task.json.SingleChoiceTaskContent;
import ru.zeker.common.dto.task.json.TaskContent;
import ru.zeker.common.dto.task.response.TaskResponse;
import ru.zeker.solution.domain.model.entity.Solution;
import ru.zeker.solution.exception.SolutionBadRequestException;
import ru.zeker.solution.exception.SolutionNotFoundException;
import ru.zeker.solution.repository.SolutionRepository;
import ru.zeker.solution.service.SolutionService;
import ru.zeker.solution.service.UserProgressService;
import ru.zeker.solution.service.client.TaskClient;
import ru.zeker.solution.service.strategy.SolutionSubmissionStrategy;

import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.AssertionsForClassTypes.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyDouble;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class SolutionServiceTest {

    @Mock
    SolutionRepository repository;
    @Mock
    TaskClient taskClient;
    @Mock
    UserProgressService userProgressService;
    @Mock
    ObjectMapper objectMapper;

    // Стратегия, которая поддерживает CodingTaskContent
    @Mock
    SolutionSubmissionStrategy<CodingTaskContent> codingStrategy;

    @InjectMocks
    SolutionService solutionService;

    private UUID userId;
    private UUID taskId;
    private UUID solutionId;
    private SolutionRequest request;
    private TaskResponse taskResponse;
    private Solution solution;

    @BeforeEach
    void setUp() {
        userId = UUID.randomUUID();
        taskId = UUID.randomUUID();
        solutionId = UUID.randomUUID();

        request = SolutionRequest.builder()
                .taskId(taskId)
                .answer("System.out.println(42);")
                .build();

        taskResponse = TaskResponse.builder()
                .id(taskId)
                .title("Test task")
                .difficulty(Difficulty.EASY)
                .tags(Set.of("java", "loops"))
                .content(CodingTaskContent.builder().testCases(List.of()).build())
                .build();

        solution = Solution.builder()
                .userId(userId)
                .taskId(taskId)
                .status(SolutionStatus.SUCCESS)
                .build();
        solution.setId(UUID.randomUUID());
    }

    // ─────────────────────────────────────────────
    // submitSolution
    // ─────────────────────────────────────────────

    @Nested
    @DisplayName("submitSolution()")
    class SubmitSolutionTest {

        @BeforeEach
        void setUpStrategies() {
            // Внедряем стратегию через конструктор вручную, чтобы контролировать список
            solutionService = new SolutionService(
                    repository,
                    List.of(codingStrategy),
                    taskClient,
                    userProgressService,
                    objectMapper
            );
        }

        @Test
        @DisplayName("успешная отправка — прогресс обновляется при SUCCESS")
        void submit_success_updatesProgress() {
            when(taskClient.getTaskById(taskId)).thenReturn(taskResponse);
            when(codingStrategy.support(any(CodingTaskContent.class))).thenReturn(true);
            //noinspection unchecked
            when(((SolutionSubmissionStrategy<TaskContent>) (SolutionSubmissionStrategy<?>) codingStrategy)
                    .handle(eq(request), eq(userId), any(TaskContent.class)))
                    .thenReturn(solution);

            Solution result = solutionService.submitSolution(request, userId);

            assertThat(result).isEqualTo(solution);
            // updateProgress должен вызываться для каждого тега
            verify(userProgressService, times(taskResponse.getTags().size()))
                    .updateOrCreate(anyString(), eq(userId), anyDouble(), eq(true), anyInt());
        }

        @Test
        @DisplayName("статус PENDING — прогресс не обновляется")
        void submit_pending_doesNotUpdateProgress() {
            solution.setStatus(SolutionStatus.PENDING);

            when(taskClient.getTaskById(taskId)).thenReturn(taskResponse);
            when(codingStrategy.support(any(CodingTaskContent.class))).thenReturn(true);
            //noinspection unchecked
            when(((SolutionSubmissionStrategy<TaskContent>) (SolutionSubmissionStrategy<?>) codingStrategy)
                    .handle(any(), any(), any()))
                    .thenReturn(solution);

            solutionService.submitSolution(request, userId);

            verifyNoInteractions(userProgressService);
        }

        @Test
        @DisplayName("нет стратегии для типа задачи — SolutionBadRequestException")
        void submit_noStrategy_throwsBadRequest() {
            TaskResponse unsupportedTask = TaskResponse.builder()
                    .id(taskId)
                    .difficulty(Difficulty.EASY)
                    .tags(Set.of())
                    .content(mock(SingleChoiceTaskContent.class))
                    .build();

            when(taskClient.getTaskById(taskId)).thenReturn(unsupportedTask);
            when(codingStrategy.support(any())).thenReturn(false);

            assertThatThrownBy(() -> solutionService.submitSolution(request, userId))
                    .isInstanceOf(SolutionBadRequestException.class)
                    .hasMessageContaining("No submission strategy available");
        }
    }

    // ─────────────────────────────────────────────
    // getSolution
    // ─────────────────────────────────────────────

    @Nested
    @DisplayName("getSolution()")
    class GetSolutionTest {

        @Test
        @DisplayName("владелец получает своё решение")
        void getSolution_owner_returnsSolution() {
            when(repository.findById(solutionId)).thenReturn(Optional.of(solution));

            Solution result = solutionService.getSolution(solutionId, userId);

            assertThat(result).isEqualTo(solution);
        }

        @Test
        @DisplayName("чужой userId — SolutionNotFoundException")
        void getSolution_notOwner_throwsNotFound() {
            when(repository.findById(solutionId)).thenReturn(Optional.of(solution));

            assertThatThrownBy(() -> solutionService.getSolution(solutionId, UUID.randomUUID()))
                    .isInstanceOf(SolutionNotFoundException.class);
        }

        @Test
        @DisplayName("решение не найдено — SolutionNotFoundException")
        void getSolution_missing_throwsNotFound() {
            when(repository.findById(solutionId)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> solutionService.getSolution(solutionId, userId))
                    .isInstanceOf(SolutionNotFoundException.class);
        }
    }

    // ─────────────────────────────────────────────
    // getUserSolutions
    // ─────────────────────────────────────────────

    @Nested
    @DisplayName("getUserSolutions()")
    class GetUserSolutionsTest {

        @Test
        @DisplayName("возвращает список решений пользователя")
        void getUserSolutions_returnsList() {
            when(repository.findByUserId(userId)).thenReturn(List.of(solution));

            List<Solution> result = solutionService.getUserSolutions(userId);

            assertThat(result).containsExactly(solution);
        }

        @Test
        @DisplayName("пустой список если решений нет")
        void getUserSolutions_empty_returnsEmptyList() {
            when(repository.findByUserId(userId)).thenReturn(List.of());

            assertThat(solutionService.getUserSolutions(userId)).isEmpty();
        }
    }

    // ─────────────────────────────────────────────
    // updateSolutionStatus
    // ─────────────────────────────────────────────

    @Nested
    @DisplayName("updateSolutionStatus()")
    class UpdateSolutionStatusTest {

        @Test
        @DisplayName("PENDING → SUCCESS: статус и обратная связь сохраняются")
        void update_pendingToSuccess_savesCalled() throws JsonProcessingException {
            solution.setStatus(SolutionStatus.PENDING);
            SolutionExecResult execResult = SolutionExecResult.builder()
                    .status(SolutionStatus.SUCCESS)
                    .descriptionError(null)
                    .build();

            when(repository.findById(solutionId)).thenReturn(Optional.of(solution));

            solutionService.updateSolutionStatus(solutionId, execResult);

            assertThat(solution.getStatus()).isEqualTo(SolutionStatus.SUCCESS);
            verify(repository).save(solution);
        }

        @Test
        @DisplayName("PENDING с ошибкой: feedback записывается в JSON")
        void update_pendingWithError_feedbackSerialized() throws JsonProcessingException {
            solution.setStatus(SolutionStatus.PENDING);
            String errorMsg = "Compilation error on line 5";
            SolutionExecResult execResult = SolutionExecResult.builder()
                    .status(SolutionStatus.FAILED)
                    .descriptionError(errorMsg)
                    .build();

            when(repository.findById(solutionId)).thenReturn(Optional.of(solution));
            when(objectMapper.writeValueAsString(errorMsg)).thenReturn("\"" + errorMsg + "\"");

            solutionService.updateSolutionStatus(solutionId, execResult);

            assertThat(solution.getFeedback()).isEqualTo("\"" + errorMsg + "\"");
            verify(repository).save(solution);
        }

        @Test
        @DisplayName("не-PENDING решение: обновление пропускается, save не вызывается")
        void update_nonPending_skipped() throws JsonProcessingException {
            solution.setStatus(SolutionStatus.SUCCESS); // уже финальный статус
            SolutionExecResult execResult = SolutionExecResult.builder()
                    .status(SolutionStatus.FAILED)
                    .build();

            when(repository.findById(solutionId)).thenReturn(Optional.of(solution));

            solutionService.updateSolutionStatus(solutionId, execResult);

            // Статус не должен измениться и save не должен вызываться
            assertThat(solution.getStatus()).isEqualTo(SolutionStatus.SUCCESS);
            verify(repository, never()).save(any());
        }

        @Test
        @DisplayName("решение не найдено — SolutionNotFoundException")
        void update_notFound_throws() {
            when(repository.findById(solutionId)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> solutionService.updateSolutionStatus(
                    solutionId,
                    SolutionExecResult.builder().status(SolutionStatus.SUCCESS).build()
            )).isInstanceOf(SolutionNotFoundException.class);
        }
    }

    // ─────────────────────────────────────────────
    // countUserSolutions / countUserSuccessfulSolutions
    // ─────────────────────────────────────────────

    @Nested
    @DisplayName("count*()")
    class CountTest {

        @Test
        @DisplayName("countUserSolutions возвращает значение репозитория")
        void countUserSolutions() {
            when(repository.countByUserId(userId)).thenReturn(42L);

            assertThat(solutionService.countUserSolutions(userId)).isEqualTo(42L);
        }

        @Test
        @DisplayName("countUserSuccessfulSolutions возвращает значение репозитория")
        void countUserSuccessfulSolutions() {
            when(repository.countByUserIdAndStatus(userId, SolutionStatus.SUCCESS)).thenReturn(10L);

            assertThat(solutionService.countUserSuccessfulSolutions(userId)).isEqualTo(10L);
        }
    }

    // ─────────────────────────────────────────────
    // checkAndTimeoutStaleSolutions
    // ─────────────────────────────────────────────

    @Nested
    @DisplayName("checkAndTimeoutStaleSolutions()")
    class TimeoutTest {

        @Test
        @DisplayName("устаревшие PENDING решения помечаются TIMEOUT и сохраняются")
        void timeout_staleSolutions_markedAndSaved() throws JsonProcessingException {
            Solution staleSolution = Solution.builder()
                    .userId(userId)
                    .status(SolutionStatus.PENDING)
                    .build();
            staleSolution.setId(UUID.randomUUID());

            when(repository.findByStatusAndCreatedAtBefore(eq(SolutionStatus.PENDING), any()))
                    .thenReturn(List.of(staleSolution));
            when(objectMapper.writeValueAsString(anyString())).thenReturn("\"timeout\"");

            solutionService.checkAndTimeoutStaleSolutions();

            assertThat(staleSolution.getStatus()).isEqualTo(SolutionStatus.TIMEOUT);
            assertThat(staleSolution.getFeedback()).isEqualTo("\"timeout\"");

            ArgumentCaptor<List<Solution>> captor = ArgumentCaptor.forClass(List.class);
            verify(repository).saveAll(captor.capture());
            assertThat(captor.getValue()).containsExactly(staleSolution);
        }

        @Test
        @DisplayName("нет устаревших решений — saveAll не вызывается")
        void timeout_noStaleSolutions_nothingSaved() {
            when(repository.findByStatusAndCreatedAtBefore(any(), any()))
                    .thenReturn(List.of());

            solutionService.checkAndTimeoutStaleSolutions();

            verify(repository, never()).saveAll(any());
        }
    }
}
