package ru.zeker.authentication.exception;

import org.springframework.http.HttpStatus;
import ru.zeker.common.exception.BaseException;
import ru.zeker.common.exception.ErrorCode;

public class OAuth2ProviderException extends BaseException {
    public OAuth2ProviderException(String message) {
        super(message, HttpStatus.BAD_REQUEST, ErrorCode.O_AUTH2_PROVIDER);
    }
    public OAuth2ProviderException() {
        super("An error occurred in the OAuth2 provider", HttpStatus.BAD_REQUEST, ErrorCode.O_AUTH2_PROVIDER);
    }
}
