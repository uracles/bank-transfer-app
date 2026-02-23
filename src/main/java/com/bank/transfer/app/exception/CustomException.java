package com.bank.transfer.app.exception;

import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
public class CustomException extends RuntimeException {
    private final HttpStatus httpStatus;
    private final String message;
    private final String code;

    public CustomException(String message) {
        super(message);
        this.code = getCode();
        this.httpStatus = getHttpStatus();
        this.message = message;
    }

    public CustomException(HttpStatus httpStatus, String code, String message) {
        super(message);
        this.code = code;
        this.httpStatus = httpStatus;
        this.message = message;
    }

    public CustomException(HttpStatus httpStatus, String message) {
        super(message);
        this.httpStatus = httpStatus;
        this.message = message;
        this.code = getCode();
    }


}
