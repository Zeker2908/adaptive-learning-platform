package ru.zeker.common.controller;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.ConstraintViolationException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.support.DefaultMessageSourceResolvable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.validation.FieldError;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingRequestCookieException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.servlet.resource.NoResourceFoundException;
import ru.zeker.common.exception.BaseException;
import ru.zeker.common.exception.ErrorCode;

import java.time.Instant;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.stream.Collectors;

@Slf4j
public class GlobalExceptionHandler {
    private Map<String, Object> buildBaseErrorResponse(HttpStatus status, String message, String path, String requestId, ErrorCode errorCode) {
        var response = new LinkedHashMap<String, Object>();
        response.put("timestamp", Instant.now().toString());
        response.put("path", path);
        response.put("status", status.value());
        response.put("error", status.getReasonPhrase());
        response.put("errorCode", errorCode);
        response.put("message", message);
        response.put("requestId", requestId);
        return response;
    }

    protected ResponseEntity<Map<String, Object>> buildErrorResponse(
            HttpStatus status,
            String message,
            String path,
            String requestId,
            ErrorCode errorCode
    ) {
        var errorResponse = buildBaseErrorResponse(status, message, path, requestId, errorCode);
        log.error("Error: {} - {}", status, message);
        return ResponseEntity.status(status).body(errorResponse);
    }

    protected ResponseEntity<Map<String, Object>> buildErrorResponse(
            HttpStatus status,
            String message,
            String path,
            String requestId
    ) {
        return buildErrorResponse(status, message, path, requestId, ErrorCode.INTERNAL_SERVER_ERROR);
    }

    @ExceptionHandler(BaseException.class)
    public ResponseEntity<Map<String, Object>> handleApiException(BaseException ex, HttpServletRequest request) {
        return buildErrorResponse(ex.getStatus(), ex.getMessage(), request.getRequestURI(), request.getRequestId(), ex.getErrorCode());
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<Map<String, Object>> handleIllegalArgumentException(IllegalArgumentException ex, HttpServletRequest request) {
        return buildErrorResponse(HttpStatus.BAD_REQUEST, ex.getMessage(), request.getRequestURI(), request.getRequestId());
    }

    @ExceptionHandler(IllegalStateException.class)
    public ResponseEntity<Map<String, Object>> handleIllegalStateException(IllegalStateException ex, HttpServletRequest request) {
        return buildErrorResponse(HttpStatus.BAD_REQUEST, ex.getMessage(), request.getRequestURI(), request.getRequestId());
    }

    @ExceptionHandler(NoResourceFoundException.class)
    public ResponseEntity<Map<String, Object>> handleNoResourceFoundException(NoResourceFoundException ex, HttpServletRequest request) {
        return buildErrorResponse(HttpStatus.NOT_FOUND, ex.getMessage(), request.getRequestURI(), request.getRequestId());
    }

    @ExceptionHandler(HttpRequestMethodNotSupportedException.class)
    public ResponseEntity<Map<String, Object>> handleHttpRequestMethodNotSupportedException(HttpRequestMethodNotSupportedException ex,
                                                                                            HttpServletRequest request) {
        return buildErrorResponse(HttpStatus.METHOD_NOT_ALLOWED, ex.getMessage(), request.getRequestURI(), request.getRequestId());
    }

    @ExceptionHandler(MissingRequestCookieException.class)
    public ResponseEntity<Map<String, Object>> handleMissingRequestCookieException(MissingRequestCookieException ex, HttpServletRequest request) {
        return buildErrorResponse(HttpStatus.BAD_REQUEST, String.format("Required cookie parameter '%s' is missing", ex.getCookieName()), request.getRequestURI(), request.getRequestId());
    }

    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<Map<String, Object>> handleHttpMessageNotReadable(HttpServletRequest request) {
        return buildErrorResponse(HttpStatus.BAD_REQUEST, "Incorrect request body format", request.getRequestURI(), request.getRequestId());
    }

    @ExceptionHandler(MissingServletRequestParameterException.class)
    public ResponseEntity<Map<String, Object>> handleMissingParams(MissingServletRequestParameterException ex, HttpServletRequest request) {
        String message = String.format("Required parameter '%s' is missing", ex.getParameterName());
        return buildErrorResponse(HttpStatus.BAD_REQUEST, message, request.getRequestURI(), request.getRequestId(), ErrorCode.MISSING_PARAMETER);
    }

    @ExceptionHandler(ConstraintViolationException.class)
    public ResponseEntity<Map<String, Object>> handleConstraintViolationException(
            ConstraintViolationException ex,
            HttpServletRequest request
    ) {
        var errors = ex.getConstraintViolations().stream()
                .collect(Collectors.toMap(
                        v -> v.getPropertyPath().toString(),
                        ConstraintViolation::getMessage
                ));
        var errorResponse = buildBaseErrorResponse(HttpStatus.BAD_REQUEST,
                "Parameter validation error", request.getRequestURI(), request.getRequestId(), ErrorCode.INVALID_INPUT);
        errorResponse.put("details", errors);

        log.error("Parameter validation error: {}", errors);
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errorResponse);
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<Map<String, Object>> handleValidationException(MethodArgumentNotValidException ex, HttpServletRequest request) {
        var validationErrors = ex.getBindingResult()
                .getFieldErrors()
                .stream()
                .collect(Collectors.toMap(
                        FieldError::getField,
                        DefaultMessageSourceResolvable::getDefaultMessage,
                        (existing, replacement) -> existing
                ));
        var errorResponse = buildBaseErrorResponse(HttpStatus.BAD_REQUEST,
                "Parameter validation error", request.getRequestURI(), request.getRequestId(), ErrorCode.INVALID_INPUT);
        errorResponse.put("details", validationErrors);

        log.error("Validation error: {}", validationErrors);
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errorResponse);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<Map<String, Object>> handleGenericException(Exception ex, HttpServletRequest request) {
        log.error("Unhandled exception", ex);
        return buildErrorResponse(
                HttpStatus.INTERNAL_SERVER_ERROR,
                "An internal server error occurred. Please try again later",
                request.getRequestURI(), request.getRequestId()
        );
    }
}
