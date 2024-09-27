package com.springboot.noticeboard.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.http.HttpStatus;
//
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class ResponseMessage {

    private ResponseMessageHeader header;
    private Object body;

    public static ResponseMessage fail(String message, Object data) {

        return ResponseMessage.builder()
                .header(ResponseMessageHeader.builder()
                        .result(false)
                        .resultCode(HttpStatus.BAD_REQUEST.getReasonPhrase())
                        .message(message)
                        .status(HttpStatus.BAD_REQUEST.value())
                        .build())
                .body(data)
                .build();
    }

    public static ResponseMessage success(String message, Object data) {
        return ResponseMessage.builder()
                .header(ResponseMessageHeader.builder()
                        .result(true)
                        .resultCode(HttpStatus.OK.getReasonPhrase())
                        .message(message)
                        .status(HttpStatus.OK.value())
                        .build())
                .body(data)
                .build();
    }

}
