package com.springboot.noticeboard.dto.response;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Builder
public class getCommentsByPostIdDTO {
    private Long id;
    private String content;
    private String author;
    private LocalDateTime createDate;
}
