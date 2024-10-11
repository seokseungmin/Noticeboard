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
public class UpdateCommentDTO {  // 이름을 UpdateCommentDTO로 명확하게 변경
    @NotBlank(message = "댓글 내용은 필수입니다.")
    private String content;  // 댓글 수정 시 필요한 것은 내용뿐
}
