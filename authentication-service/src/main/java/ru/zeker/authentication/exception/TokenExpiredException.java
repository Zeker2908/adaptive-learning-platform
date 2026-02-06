package ru.zeker.authentication.exception;

import org.springframework.http.HttpStatus;
import ru.zeker.common.exception.BaseException;
import ru.zeker.common.exception.ErrorCode;

public class TokenExpiredException extends BaseException {
    public TokenExpiredException(String message) {
        super(message, HttpStatus.UNAUTHORIZED, ErrorCode.REFRESH_TOKEN_EXPIRED);
    }
}
