package com.springboot.noticeboard.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
//
@Data
@NoArgsConstructor
@AllArgsConstructor
public class UpdatePostDTO {
    @NotBlank(message = "제목은 필수 입력 사항입니다.")
    @Size(max = 20, message = "제목은 최대 20자까지 가능합니다.")
    private String title;

    @NotBlank(message = "내용은 필수 입력 사항입니다.")
    @Size(max = 1000, message = "내용은 최대 1000자까지 가능합니다.")
    private String content;
}
