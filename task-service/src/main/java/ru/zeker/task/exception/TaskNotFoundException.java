package ru.zeker.task.exception;

import org.springframework.http.HttpStatus;
import ru.zeker.common.exception.BaseException;

public class TaskNotFoundException extends BaseException {
    public TaskNotFoundException(String message) {
        super(message, HttpStatus.NOT_FOUND);
    }

    public TaskNotFoundException() {
        super("Task not found", HttpStatus.NOT_FOUND);
    }
}

