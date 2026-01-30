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

import java.nio.file.AccessDeniedException;
import java.util.Map;

@RestControllerAdvice
public class AuthenticationExceptionHandler extends GlobalExceptionHandler {
    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    public ResponseEntity<Map<String, Object>> handleTypeMismatch(MethodArgumentTypeMismatchException ex, HttpServletRequest request) {
        return buildErrorResponse(HttpStatus.BAD_REQUEST, "Некорректное значение параметра", request.getRequestURI(), request.getRequestId());
    }

    @ExceptionHandler(BadCredentialsException.class)
    public ResponseEntity<Map<String, Object>> handleCredentialsException(BadCredentialsException ex, HttpServletRequest request) {
        return buildErrorResponse(HttpStatus.BAD_REQUEST, "Неправильный логин или пароль", request.getRequestURI(), request.getRequestId());
    }

    @ExceptionHandler(AccessDeniedException.class)
    public ResponseEntity<Map<String, Object>> handleAccessDeniedException(AccessDeniedException ex, HttpServletRequest request) {
        return buildErrorResponse(HttpStatus.FORBIDDEN, "Доступ запрещен", request.getRequestURI(), request.getRequestId());
    }

    @ExceptionHandler(LockedException.class)
    public ResponseEntity<Map<String, Object>> handleLockedException(LockedException ex, HttpServletRequest request) {
        return buildErrorResponse(HttpStatus.LOCKED, "Аккаунт пользователя заблокирован", request.getRequestURI(), request.getRequestId());
    }

    @ExceptionHandler(AuthenticationException.class)
    public ResponseEntity<Map<String, Object>> handleAuthenticationException(AuthenticationException ex, HttpServletRequest request) {
        return buildErrorResponse(HttpStatus.UNAUTHORIZED, ex.getMessage(), request.getRequestURI(), request.getRequestId());
    }

    @ExceptionHandler(SignatureException.class)
    public ResponseEntity<Map<String, Object>> handleSignatureException(SignatureException ex, HttpServletRequest request) {
        return buildErrorResponse(HttpStatus.UNAUTHORIZED, "Недействительный токен", request.getRequestURI(), request.getRequestId());
    }

    @ExceptionHandler({CredentialsExpiredException.class, DisabledException.class, AccountExpiredException.class})
    public ResponseEntity<Map<String, Object>> handleAccountStatusExceptions(RuntimeException ex, HttpServletRequest request) {
        HttpStatus status = ex instanceof CredentialsExpiredException
                ? HttpStatus.UNAUTHORIZED
                : HttpStatus.FORBIDDEN;

        return buildErrorResponse(status, ex.getMessage(), request.getRequestURI(), request.getRequestId());
    }
}
