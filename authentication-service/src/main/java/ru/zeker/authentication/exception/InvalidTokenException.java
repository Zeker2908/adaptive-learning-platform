package ru.zeker.authentication.exception;

import org.springframework.http.HttpStatus;
import ru.zeker.common.exception.BaseException;
import ru.zeker.common.exception.ErrorCode;

public class InvalidTokenException extends BaseException {
    public InvalidTokenException(String message) {
        super(message, HttpStatus.BAD_REQUEST, ErrorCode.AUTHORIZATION_ERROR);
    }

    public InvalidTokenException() {
        super("Token is invalid", HttpStatus.BAD_REQUEST, ErrorCode.AUTHORIZATION_ERROR);
    }

    public InvalidTokenException(String message, ErrorCode errorCode) {
        super(message, HttpStatus.BAD_REQUEST, errorCode);
    }
}
