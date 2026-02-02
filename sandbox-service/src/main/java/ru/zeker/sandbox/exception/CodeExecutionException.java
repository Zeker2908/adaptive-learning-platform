package ru.zeker.sandbox.exception;

import org.springframework.http.HttpStatus;
import ru.zeker.common.exception.BaseException;

public class CodeExecutionException extends BaseException {
    public CodeExecutionException(String message){
        super(message, HttpStatus.UNPROCESSABLE_ENTITY);
    }
}
