package ru.zeker.solution.exception;

import org.springframework.http.HttpStatus;
import ru.zeker.common.exception.BaseException;
import ru.zeker.common.exception.ErrorCode;

public class SolutionNotFoundException extends BaseException {

    public SolutionNotFoundException() {
        super("No solution found", HttpStatus.NOT_FOUND, ErrorCode.SOLUTION_NOT_FOUND);
    }
}
