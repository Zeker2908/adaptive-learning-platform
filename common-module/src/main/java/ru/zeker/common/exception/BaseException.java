package ru.zeker.common.exception;

import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
public class BaseException extends RuntimeException {
  private final HttpStatus status;
  private final ErrorCode errorCode;

  public BaseException(String message, HttpStatus status, ErrorCode errorCode) {
    super(message);
    this.status = status;
    this.errorCode = errorCode;
  }
}
