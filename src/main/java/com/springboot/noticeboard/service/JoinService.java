package com.springboot.noticeboard.service;

import com.springboot.noticeboard.dto.request.JoinDTO;
import com.springboot.noticeboard.dto.response.ServiceResult;
import com.springboot.noticeboard.entity.UserEntity;
import com.springboot.noticeboard.exception.BizException;
import com.springboot.noticeboard.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class JoinService {
    private final UserRepository userRepository;
    private final BCryptPasswordEncoder passwordEncoder;

    public ServiceResult joinProcess(JoinDTO joinDTO) {
        // 이메일 중복 체크
        if (userRepository.existsByEmail(joinDTO.getEmail())) {
            throw new BizException("이미 존재하는 이메일입니다.", HttpStatus.CONFLICT);
        }
        // 사용자 엔티티 생성 및 저장
        userRepository.save(buildUser(joinDTO));
        // 회원가입 성공 시 201 Created 상태 코드와 메시지 반환
        return ServiceResult.success(HttpStatus.CREATED, "회원 가입에 성공했습니다!");
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