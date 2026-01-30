package ru.zeker.sandbox.exception;

import org.springframework.http.HttpStatus;
import ru.zeker.common.exception.ApiException;
import ru.zeker.sandbox.service.CodeExecutionService;

public class CodeExecutionException extends ApiException {
    public CodeExecutionException(String message){
        super(message, HttpStatus.UNPROCESSABLE_ENTITY);
    }
}
