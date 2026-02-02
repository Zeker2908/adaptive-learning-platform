package ru.zeker.task.controller;

import com.fasterxml.jackson.databind.exc.InvalidTypeIdException;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import ru.zeker.common.controller.GlobalExceptionHandler;

import java.util.Map;

@RestControllerAdvice
public class TaskExceptionHandler extends GlobalExceptionHandler {
    @ExceptionHandler(InvalidTypeIdException.class)
    public ResponseEntity<Map<String, Object>> handleInvalidType(InvalidTypeIdException ex, HttpServletRequest request) {
        return buildErrorResponse(HttpStatus.BAD_REQUEST, "Invalid task type: " + ex.getTypeId(), request.getRequestURI(), request.getRequestId());
    }
}
