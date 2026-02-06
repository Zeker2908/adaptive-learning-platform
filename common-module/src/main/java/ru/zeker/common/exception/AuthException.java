package ru.zeker.common.exception;

import lombok.Getter;
import org.springframework.http.HttpStatus;

import static ru.zeker.common.exception.ErrorCode.AUTHORIZATION_ERROR;

@Getter
public class AuthException extends BaseException {

    public AuthException(String message) {
        super(message, HttpStatus.UNAUTHORIZED, AUTHORIZATION_ERROR);
    }

    public AuthException(String message, HttpStatus status) {
        super(message, status, AUTHORIZATION_ERROR);
    }

    public AuthException(String message, ErrorCode errorCode) {
        super(message, HttpStatus.UNAUTHORIZED, errorCode);
    }

    public AuthException(String message, HttpStatus httpStatus, ErrorCode errorCode) {
        super(message, httpStatus, errorCode);
    }
}