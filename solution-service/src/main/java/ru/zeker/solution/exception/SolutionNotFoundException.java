package ru.zeker.solution.exception;

import org.springframework.http.HttpStatus;
import ru.zeker.common.exception.ApiException;

public class SolutionNotFoundException extends ApiException {
    public SolutionNotFoundException(String message) {
        super(message, HttpStatus.NOT_FOUND);
    }

    public SolutionNotFoundException() {
        super("Решение не найдено", HttpStatus.NOT_FOUND);
    }
}
