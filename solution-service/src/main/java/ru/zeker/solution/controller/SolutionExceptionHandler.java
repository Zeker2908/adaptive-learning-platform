package ru.zeker.solution.controller;

import feign.FeignException;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
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
            String body = ex.contentUTF8();
            if (StringUtils.isNotBlank(body)) {
                message = body;
            }
        } catch (Exception ignored) {
        }

        log.error("Feign exception: HTTP {} - {}", ex.status(), message, ex);
        return buildErrorResponse(
                status,
                "Error calling external service: " + message,
                request.getRequestURI(),
                request.getRequestId()
        );
    }
}
