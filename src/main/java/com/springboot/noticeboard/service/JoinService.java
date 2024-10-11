package com.springboot.noticeboard.service;

import com.springboot.noticeboard.dto.request.JoinDTO;
import com.springboot.noticeboard.dto.response.ServiceResult;
import com.springboot.noticeboard.entity.UserEntity;
import com.springboot.noticeboard.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class JoinService {
    private final UserRepository userRepository;
    private final BCryptPasswordEncoder passwordEncoder;

    public ServiceResult joinProcess(JoinDTO joinDTO) {
        // 사용자 엔티티 생성 및 저장
        userRepository.save(buildUser(joinDTO));
        return ServiceResult.success("회원 가입에 성공했습니다!");
    }

    // UserEntity 빌더를 통한 엔티티 생성
    private UserEntity buildUser(JoinDTO joinDTO) {
        return UserEntity.builder()
                .username(joinDTO.getUsername())
                .password(passwordEncoder.encode(joinDTO.getPassword()))
                .email(joinDTO.getEmail())
                .role(joinDTO.getRole())
                .build();
    }
}