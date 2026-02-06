package ru.zeker.authentication.exception;

import org.springframework.http.HttpStatus;
import ru.zeker.common.exception.BaseException;
import ru.zeker.common.exception.ErrorCode;

public class UserAlreadyEnableException extends BaseException {
    public UserAlreadyEnableException() {
        super("The user is already activated", HttpStatus.CONFLICT, ErrorCode.USER_ALREADY_ACTIVATION);
    }
}
