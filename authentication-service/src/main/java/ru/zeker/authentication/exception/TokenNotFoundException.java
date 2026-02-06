package ru.zeker.authentication.exception;

import org.springframework.http.HttpStatus;
import ru.zeker.common.exception.BaseException;
import ru.zeker.common.exception.ErrorCode;

public class TokenNotFoundException extends BaseException {
    public TokenNotFoundException(String message) {
        super(message, HttpStatus.NOT_FOUND, ErrorCode.REFRESH_TOKEN_NOT_FOUND);
    }
}
