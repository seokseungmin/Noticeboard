package com.springboot.noticeboard.dto.request;

import com.springboot.noticeboard.type.Role;
import jakarta.annotation.Nonnull;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.*;

//
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class JoinDTO {

    @NotBlank(message = "사용자 이름은 필수 입니다.")
    private String username;

    @Size(min = 4, message = "비밀번호는 4자 이상 입력해야 합니다.")
    @NotBlank(message = "비밀번호는 필수 입니다.")
    private String password;

    @Email(message = "이메일 형식이 잘못되었습니다.")
    @NotBlank(message = "이메일 항목은 필수 입니다.")
    private String email;

    @NotNull(message = "역할은 필수 항목입니다.")
    private Role role;
}