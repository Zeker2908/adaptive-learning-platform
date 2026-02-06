package ru.zeker.authentication.exception;

import org.springframework.http.HttpStatus;
import ru.zeker.common.exception.BaseException;
import ru.zeker.common.exception.ErrorCode;

public class LocalAuthUserNotFoundException extends BaseException {
    public LocalAuthUserNotFoundException(String message) {
        super(message, HttpStatus.NOT_FOUND, ErrorCode.LOCAL_AUTH_NOT_FOUND);
    }

    public LocalAuthUserNotFoundException() {
        super("LocalAuth not found for user", HttpStatus.NOT_FOUND, ErrorCode.LOCAL_AUTH_NOT_FOUND);
    }
}
