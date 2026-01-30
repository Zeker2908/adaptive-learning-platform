package ru.zeker.solution.controller;

import feign.FeignException;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import ru.zeker.common.controller.GlobalExceptionHandler;

import java.util.Map;

@Slf4j
@RestControllerAdvice
public class SolutionExceptionHandler extends GlobalExceptionHandler {
    @ExceptionHandler(FeignException.class)
    public ResponseEntity<Map<String, Object>> handleFeignException(FeignException ex, HttpServletRequest request) {
        HttpStatus status = HttpStatus.resolve(ex.status());

        String message = ex.getMessage();
        try {
            // Иногда Feign возвращает тело с сообщением, можно попытаться распарсить
            String body = ex.contentUTF8();
            if (body != null && !body.isBlank()) {
                message = body;
            }
        } catch (Exception e) {
            // игнорируем парсинг ошибок
        }

        log.error("Feign exception: HTTP {} - {}", ex.status(), message, ex);
        return buildErrorResponse(
                status,
                "Ошибка при вызове внешнего сервиса: " + message,
                request.getRequestURI(),
                request.getRequestId()
        );
    }
}
