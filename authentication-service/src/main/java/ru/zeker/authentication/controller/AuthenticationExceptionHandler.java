package ru.zeker.authentication.controller;

import io.jsonwebtoken.security.SignatureException;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.messaging.handler.annotation.support.MethodArgumentTypeMismatchException;
import org.springframework.security.authentication.AccountExpiredException;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.CredentialsExpiredException;
import org.springframework.security.authentication.DisabledException;
import org.springframework.security.authentication.LockedException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import ru.zeker.common.controller.GlobalExceptionHandler;
import ru.zeker.common.exception.ErrorCode;

import java.nio.file.AccessDeniedException;
import java.util.Map;

@RestControllerAdvice
public class AuthenticationExceptionHandler extends GlobalExceptionHandler {
    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    public ResponseEntity<Map<String, Object>> handleTypeMismatch(HttpServletRequest request) {
        return buildErrorResponse(HttpStatus.BAD_REQUEST, "Incorrect parameter value", request.getRequestURI(), request.getRequestId(), ErrorCode.INVALID_INPUT);
    }

    @ExceptionHandler(BadCredentialsException.class)
    public ResponseEntity<Map<String, Object>> handleCredentialsException(HttpServletRequest request) {
        return buildErrorResponse(HttpStatus.BAD_REQUEST, "Incorrect login or password", request.getRequestURI(), request.getRequestId(), ErrorCode.BAD_CREDENTIALS);
    }

    @ExceptionHandler(AccessDeniedException.class)
    public ResponseEntity<Map<String, Object>> handleAccessDeniedException(HttpServletRequest request) {
        return buildErrorResponse(HttpStatus.FORBIDDEN, "Access denied", request.getRequestURI(), request.getRequestId(), ErrorCode.ACCESS_DENIED);
    }

    @ExceptionHandler(LockedException.class)
    public ResponseEntity<Map<String, Object>> handleLockedException(HttpServletRequest request) {
        return buildErrorResponse(HttpStatus.LOCKED, "The user's account has been blocked", request.getRequestURI(), request.getRequestId(), ErrorCode.ACCOUNT_BLOCKED);
    }

    @ExceptionHandler(DisabledException.class)
    public ResponseEntity<Map<String, Object>> handleDisabledException(HttpServletRequest request) {
        return buildErrorResponse(HttpStatus.FORBIDDEN, "The user's account has been created but not activated.", request.getRequestURI(), request.getRequestId(), ErrorCode.ACCOUNT_DISABLED);
    }

    @ExceptionHandler(AuthenticationException.class)
    public ResponseEntity<Map<String, Object>> handleAuthenticationException(AuthenticationException ex, HttpServletRequest request) {
        return buildErrorResponse(HttpStatus.UNAUTHORIZED, ex.getMessage(), request.getRequestURI(), request.getRequestId(), ErrorCode.AUTHORIZATION_ERROR);
    }

    @ExceptionHandler(SignatureException.class)
    public ResponseEntity<Map<String, Object>> handleSignatureException(HttpServletRequest request) {
        return buildErrorResponse(HttpStatus.UNAUTHORIZED, "Invalid token", request.getRequestURI(), request.getRequestId(), ErrorCode.AUTHORIZATION_ERROR);
    }

    @ExceptionHandler({CredentialsExpiredException.class, AccountExpiredException.class})
    public ResponseEntity<Map<String, Object>> handleAccountStatusExceptions(RuntimeException ex, HttpServletRequest request) {
        var status = ex instanceof CredentialsExpiredException
                ? HttpStatus.UNAUTHORIZED
                : HttpStatus.FORBIDDEN;

        return buildErrorResponse(status, ex.getMessage(), request.getRequestURI(), request.getRequestId(), ErrorCode.AUTHORIZATION_ERROR);
    }
}
