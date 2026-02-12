package ru.zeker.authentication.exception;

import org.springframework.http.HttpStatus;
import ru.zeker.common.exception.BaseException;
import ru.zeker.common.exception.ErrorCode;

public class AdminNoneBlockException extends BaseException {

    public AdminNoneBlockException(){
        super("Administrator cannot block himself", HttpStatus.BAD_REQUEST, ErrorCode.SELF_BLOCK_FORBIDDEN);
    }
    public AdminNoneBlockException(String message, HttpStatus status, ErrorCode errorCode) {
        super(message, status, errorCode);
    }
}
