package ru.zeker.authentication.exception;

import org.springframework.http.HttpStatus;
import ru.zeker.common.exception.BaseException;
import ru.zeker.common.exception.ErrorCode;

public class TooManyRequestsException extends BaseException {
    public TooManyRequestsException() {
        super("Too many requests", HttpStatus.TOO_MANY_REQUESTS, ErrorCode.TOO_MANY_RESEND_VERIFICATION);
    }
}
