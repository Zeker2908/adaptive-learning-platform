package ru.zeker.authentication.exception;

import org.springframework.http.HttpStatus;
import ru.zeker.common.exception.BaseException;

public class InvalidTokenException extends BaseException {
  public InvalidTokenException(String message) {
    super(message, HttpStatus.BAD_REQUEST);
  }
  public InvalidTokenException() {
    super("Token is invalid", HttpStatus.BAD_REQUEST);
  }
}
