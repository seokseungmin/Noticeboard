package com.springboot.noticeboard.exception;

import org.springframework.http.HttpStatus;

public class BizException extends RuntimeException {
    private final HttpStatus status;

    public BizException(String message, HttpStatus status) {
        super(message);
        this.status = status;
    }

    public HttpStatus getStatus() {
        return status;
    }
}
