package ru.zeker.solution.exception;

import org.springframework.http.HttpStatus;
import ru.zeker.common.exception.ApiException;

public class SolutionBadRequestException extends ApiException {
    public SolutionBadRequestException(String message) {
        super(message, HttpStatus.BAD_REQUEST);
    }
}
