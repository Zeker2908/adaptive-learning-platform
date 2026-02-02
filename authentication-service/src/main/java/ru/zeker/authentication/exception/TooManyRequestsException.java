package ru.zeker.authentication.exception;

import org.springframework.http.HttpStatus;
import ru.zeker.common.exception.BaseException;

public class TooManyRequestsException extends BaseException {
    public TooManyRequestsException(String message) {
        super(message, HttpStatus.TOO_MANY_REQUESTS);
    }
    public TooManyRequestsException() {
        super("Too many requests", HttpStatus.TOO_MANY_REQUESTS);
    }
}
