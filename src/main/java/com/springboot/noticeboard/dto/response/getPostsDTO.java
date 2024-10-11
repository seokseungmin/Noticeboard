package com.springboot.noticeboard.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class getPostsDTO {
    private Long id;
    private String title;
    private LocalDateTime createDate;
    private int commentCount;
}
