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

// 전역 예외 처리 핸들러 추가
@RestControllerAdvice
public class GlobalExceptionHandler {

    // 유효성 검증 실패 시 처리
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<?> handleValidationExceptions(MethodArgumentNotValidException ex) {
        List<ResponseError> errors = ex.getBindingResult().getFieldErrors().stream()
                .map(ResponseError::of)
                .collect(Collectors.toList());
        return ResponseEntity.badRequest().body(errors);
    }

    // 커스텀 비즈니스 예외 처리
    @ExceptionHandler(BizException.class)
    public ResponseEntity<?> handleBizException(BizException ex) {
        return ResponseResult.fail(ex.getMessage());
    }

    // 그 외 모든 예외 처리
    @ExceptionHandler(Exception.class)
    public ResponseEntity<?> handleGenericException(Exception ex) {
        return ResponseResult.fail("서버 오류가 발생했습니다.");
    }
}
