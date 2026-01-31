package ru.zeker.gateway.exception;

import lombok.Getter;
import org.springframework.http.HttpStatus;
import ru.zeker.common.exception.ApiException;

@Getter
public class AuthException extends ApiException {
    private final String reason;

    public AuthException(String message, HttpStatus status) {
        super(message, status);
        this.reason = null;
    }

    public AuthException(String message, HttpStatus status, String reason) {
        super(message, status);
        this.reason = reason;
    }
}