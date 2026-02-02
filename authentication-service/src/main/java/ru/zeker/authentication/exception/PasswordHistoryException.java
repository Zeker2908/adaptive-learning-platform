package ru.zeker.authentication.exception;

import org.springframework.http.HttpStatus;
import ru.zeker.common.exception.BaseException;

public class PasswordHistoryException extends BaseException {
    public PasswordHistoryException(String message) {
        super(message, HttpStatus.BAD_REQUEST);
    }
    public PasswordHistoryException(){
        super("Incorrect password", HttpStatus.BAD_REQUEST );
    }
}
