package com.springboot.noticeboard.dto.request;

import com.springboot.noticeboard.type.Role;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class JoinDTO {
    private String username;
    private String password;
    private String email;
    private Role role;
}