package com.lms.common.exception;

import org.springframework.http.HttpStatus;

public class ForbiddenException extends LmsException {
    
    public ForbiddenException(String message) {
        super(message, HttpStatus.FORBIDDEN, "FORBIDDEN");
    }

    public ForbiddenException() {
        super("Access denied", HttpStatus.FORBIDDEN, "FORBIDDEN");
    }
}
