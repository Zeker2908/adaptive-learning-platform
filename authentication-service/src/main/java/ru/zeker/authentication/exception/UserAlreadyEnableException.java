package ru.zeker.authentication.exception;

import org.springframework.http.HttpStatus;
import ru.zeker.common.exception.BaseException;

public class UserAlreadyEnableException extends BaseException {
    public UserAlreadyEnableException(String message) {
        super(message, HttpStatus.CONFLICT);
    }
    public UserAlreadyEnableException() {
        super("The user is already activated", HttpStatus.CONFLICT);
    }
}
