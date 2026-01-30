package ru.zeker.task.exception;

import org.springframework.http.HttpStatus;
import ru.zeker.common.exception.ApiException;

public class TaskNotFoundException extends ApiException {
    public TaskNotFoundException(String message) {
        super(message, HttpStatus.NOT_FOUND);
    }

    public TaskNotFoundException() {
        super("Task not found", HttpStatus.NOT_FOUND);
    }
}

