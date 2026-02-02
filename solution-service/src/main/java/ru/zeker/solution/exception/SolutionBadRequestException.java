package ru.zeker.solution.exception;

import org.springframework.http.HttpStatus;
import ru.zeker.common.exception.BaseException;

public class SolutionBadRequestException extends BaseException {
    public SolutionBadRequestException(String message) {
        super(message, HttpStatus.BAD_REQUEST);
    }
}
