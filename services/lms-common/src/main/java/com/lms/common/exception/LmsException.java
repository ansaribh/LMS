package com.lms.common.exception;

import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
public class LmsException extends RuntimeException {
    
    private final HttpStatus status;
    private final String errorCode;

    public LmsException(String message) {
        super(message);
        this.status = HttpStatus.INTERNAL_SERVER_ERROR;
        this.errorCode = "LMS_ERROR";
    }

    public LmsException(String message, HttpStatus status) {
        super(message);
        this.status = status;
        this.errorCode = "LMS_ERROR";
    }

    public LmsException(String message, HttpStatus status, String errorCode) {
        super(message);
        this.status = status;
        this.errorCode = errorCode;
    }

    public LmsException(String message, Throwable cause) {
        super(message, cause);
        this.status = HttpStatus.INTERNAL_SERVER_ERROR;
        this.errorCode = "LMS_ERROR";
    }

    public LmsException(String message, HttpStatus status, Throwable cause) {
        super(message, cause);
        this.status = status;
        this.errorCode = "LMS_ERROR";
    }
}
