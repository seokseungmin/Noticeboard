package com.springboot.noticeboard.service;

import com.springboot.noticeboard.dto.request.JoinDTO;
import com.springboot.noticeboard.entity.UserEntity;
import com.springboot.noticeboard.repository.UserRepository;
import com.springboot.noticeboard.type.Role;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class JoinService {
    private final UserRepository userRepository;
    private final BCryptPasswordEncoder passwordEncoder;

    public void joinProcess(JoinDTO joinDTO) {
        validateEmailDuplication(joinDTO.getEmail());
        userRepository.save(buildUser(joinDTO));
    }

    // 이메일 중복 확인 로직
    private void validateEmailDuplication(String email) {
        userRepository.findByEmail(email)
                .ifPresent(user -> {
                    throw new IllegalArgumentException("계정이 이미 존재합니다.");
                });
    }

    // UserEntity 빌더를 통한 엔티티 생성
    private UserEntity buildUser(JoinDTO joinDTO) {
        return UserEntity.builder()
                .username(joinDTO.getUsername())
                .password(passwordEncoder.encode(joinDTO.getPassword()))
                .email(joinDTO.getEmail())
                .createDate(LocalDateTime.now())
                .role(joinDTO.getRole())
                .build();
    }
}