package ru.zeker.authentication.exception;

import org.springframework.http.HttpStatus;
import ru.zeker.common.exception.BaseException;
import ru.zeker.common.exception.ErrorCode;

public class PasswordHistoryException extends BaseException {
    public PasswordHistoryException(String message) {
        super(message, HttpStatus.BAD_REQUEST, ErrorCode.PASSWORD_HISTORY);
    }
}
