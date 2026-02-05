package ru.zeker.common.exception;

import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
public class AuthException extends BaseException {
    private final String reason;

    public AuthException(String message, String reason) {
        super(message, HttpStatus.UNAUTHORIZED);
        this.reason = reason;
    }

    public AuthException(String message, HttpStatus status) {
        super(message, status);
        this.reason = null;
    }

    public AuthException(String message, HttpStatus status, String reason) {
        super(message, status);
        this.reason = reason;
    }
}