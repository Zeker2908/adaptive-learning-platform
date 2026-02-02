package ru.zeker.authentication.exception;

import org.springframework.http.HttpStatus;
import ru.zeker.common.exception.BaseException;

public class OAuth2ProviderException extends BaseException {
    public OAuth2ProviderException(String message) {
        super(message, HttpStatus.BAD_REQUEST);
    }
    public OAuth2ProviderException() {
        super("An error occurred in the OAuth2 provider.", HttpStatus.BAD_REQUEST);
    }
}
