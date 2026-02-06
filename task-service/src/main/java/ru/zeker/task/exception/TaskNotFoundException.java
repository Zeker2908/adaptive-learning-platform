package ru.zeker.task.exception;

import org.springframework.http.HttpStatus;
import ru.zeker.common.exception.BaseException;
import ru.zeker.common.exception.ErrorCode;

public class TaskNotFoundException extends BaseException {

    public TaskNotFoundException() {
        super("Task not found", HttpStatus.NOT_FOUND, ErrorCode.TASK_NOT_FOUND);
    }
}

