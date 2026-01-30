package ru.zeker.sandbox.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import ru.zeker.common.dto.judge0.request.SubmissionRequest;
import ru.zeker.common.dto.judge0.response.Status;
import ru.zeker.common.dto.judge0.response.SubmissionResponse;
import ru.zeker.common.dto.kafka.solution.SolutionExecRequest;
import ru.zeker.common.dto.task.TestCase;
import ru.zeker.sandbox.client.Judge0Client;

import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.Random;

@Slf4j
@Service
@RequiredArgsConstructor
public class CodeExecutionService {

    private static final String ACCEPTED = "Accepted";
    private static final String WRONG_ANSWER = "Wrong Answer";

    private final Judge0Client judge0Client;
    private final Random random = new Random();

    public SubmissionResponse execute(SolutionExecRequest request) {
        int langId = request.getLanguage().getCode();

        // Берём ОДИН случайный тест из списка
        TestCase selectedTest = request.getTests().get(
                random.nextInt(request.getTests().size())
        );

        String stdin = selectedTest.getInput();
        String expectedOutput = selectedTest.getOutput();

        // Нормализуем переносы строк
        if (!stdin.endsWith("\n")) {
            stdin += "\n";
        }
        if (!expectedOutput.endsWith("\n")) {
            expectedOutput += "\n";
        }

        String encodedCode = Base64.getEncoder()
                .encodeToString(request.getCode().getBytes(StandardCharsets.UTF_8));
        String encodedStdin = Base64.getEncoder()
                .encodeToString(stdin.getBytes(StandardCharsets.UTF_8));

        SubmissionRequest sub = SubmissionRequest.builder()
                .sourceCode(encodedCode)
                .languageId(langId)
                .stdin(encodedStdin)
                .build();

        log.info("Executing single random test case");

        SubmissionResponse response = judge0Client.submitCode(sub, true, true);

        // Проверяем результат вручную
        if (isCorrect(response, expectedOutput)) {
            // Возвращаем успешный ответ с ID=3 (Accepted)
            return SubmissionResponse.builder()
                    .status(Status.builder()
                            .id(3)
                            .description(ACCEPTED)
                            .build())
                    .stdout(response.getStdout())
                    .time(response.getTime())
                    .memory(response.getMemory())
                    .build();
        } else {
            return SubmissionResponse.builder()
                    .status(Status.builder()
                            .id(4)
                            .description(WRONG_ANSWER)
                            .build())
                    .stdout(response.getStdout())
                    .stderr(response.getStderr())
                    .time(response.getTime())
                    .memory(response.getMemory())
                    .build();
        }
    }

    private boolean isCorrect(SubmissionResponse response, String expectedOutput) {
        if (response.getStdout() == null) {
            return false;
        }

        String actualOutput = safeDecodeBase64(response.getStdout());

        String normalizedActual = actualOutput.stripTrailing();
        String normalizedExpected = expectedOutput.stripTrailing();

        log.info("Comparing: actual='{}', expected='{}'", normalizedActual, normalizedExpected);
        return normalizedActual.equals(normalizedExpected);
    }

    private String safeDecodeBase64(String value) {
        if (value == null) return null;

        try {
            String cleaned = value.replaceAll("[^A-Za-z0-9+/=]", StringUtils.EMPTY);
            byte[] decoded = Base64.getDecoder().decode(cleaned);
            return new String(decoded, StandardCharsets.UTF_8);
        } catch (Exception e) {
            log.warn("Received non-base64 stdout (raw): {}", value);
            return value;
        }
    }
}