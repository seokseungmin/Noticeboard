package com.springboot.noticeboard.handler;

import com.springboot.noticeboard.dto.response.ResponseError;
import com.springboot.noticeboard.dto.response.ResponseResult;
import com.springboot.noticeboard.exception.BizException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.List;
import java.util.stream.Collectors;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler({
            BizException.class
    })
    public ResponseEntity<?> handleCustomExceptions(Exception exception) {
        return ResponseResult.fail(exception.getMessage());
    }
}
