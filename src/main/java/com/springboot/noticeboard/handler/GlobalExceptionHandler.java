package com.springboot.noticeboard.handler;

import com.springboot.noticeboard.dto.response.ResponseResult;
import com.springboot.noticeboard.exception.BizException;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
//
@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler({
            BizException.class
    })
    public ResponseEntity<?> handleCustomExceptions(Exception exception) {
        return ResponseResult.fail(exception.getMessage());
    }
}
