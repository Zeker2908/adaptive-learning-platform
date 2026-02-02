package ru.zeker.solution.exception;

import org.springframework.http.HttpStatus;
import ru.zeker.common.exception.BaseException;

public class SolutionNotFoundException extends BaseException {
    public SolutionNotFoundException(String message) {
        super(message, HttpStatus.NOT_FOUND);
    }

    public SolutionNotFoundException() {
        super("No solution found", HttpStatus.NOT_FOUND);
    }
}
