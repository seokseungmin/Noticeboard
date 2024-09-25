package com.springboot.noticeboard.entity;

import com.springboot.noticeboard.type.Role;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Entity
public class UserEntity extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "username")
    private String username;

    @Column(name = "password")
    private String password;

    @Column(name = "email")
    private String email;


    // JPA와의 호환성, 유지보수성 측면에서 일반적으로는 @Enumerated(EnumType.STRING)을 사용하여
    // VARCHAR로 저장하는 것이 더 안전하고 추천되는 방식.
    @Enumerated(EnumType.STRING)  // Enum을 문자열로 저장
    private Role role;
}
