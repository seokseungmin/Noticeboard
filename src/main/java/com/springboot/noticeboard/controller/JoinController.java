package com.springboot.noticeboard.controller;

import com.springboot.noticeboard.dto.request.JoinDTO;
import com.springboot.noticeboard.dto.response.ErrorResponseHandler;
import com.springboot.noticeboard.dto.response.ResponseError;
import com.springboot.noticeboard.dto.response.ResponseResult;
import com.springboot.noticeboard.dto.response.ServiceResult;
import com.springboot.noticeboard.service.JoinService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.Errors;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.List;

@RestController
@RequiredArgsConstructor
public class JoinController {
    private final JoinService joinService;

    @PostMapping("/join")
    public ResponseEntity<?> joinProcess(@RequestBody @Valid JoinDTO joinDTO, Errors errors) {

        // 에러 처리 부분을 별도 핸들러로 위임
        ResponseEntity<?> errorResponse = ErrorResponseHandler.handleValidationErrors(errors);
        if (errorResponse != null) {
            return errorResponse;
        }

        ServiceResult serviceResult = joinService.joinProcess(joinDTO);
        return ResponseResult.result(serviceResult);
    }
}