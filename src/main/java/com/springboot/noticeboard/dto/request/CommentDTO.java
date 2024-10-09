package com.springboot.noticeboard.dto.request;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class CommentDTO {

    @NotBlank(message = "댓글 내용은 필수입니다.")
    private String content;

    private Long postId;  // 댓글이 달리는 게시글 ID를 저장
}
