package com.springboot.noticeboard.service;

import com.springboot.noticeboard.dto.request.JoinDTO;
import com.springboot.noticeboard.dto.response.ServiceResult;
import com.springboot.noticeboard.entity.UserEntity;
import com.springboot.noticeboard.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
//
@Service
@RequiredArgsConstructor
public class JoinService {
    private final UserRepository userRepository;
    private final BCryptPasswordEncoder passwordEncoder;

    public ServiceResult joinProcess(JoinDTO joinDTO) {

        try {
            // 이메일 중복 확인
            validateEmailDuplication(joinDTO.getEmail());

            // 사용자 엔티티 생성 및 저장
            userRepository.save(buildUser(joinDTO));
            return ServiceResult.success("회원 가입에 성공했습니다!");

        } catch (IllegalArgumentException e) {
            // 이메일 중복과 같은 경우에 대한 처리
            return ServiceResult.fail(e.getMessage());
        } catch (Exception e) {
            // 기타 예외 처리
            return ServiceResult.fail("회원 가입 중 오류가 발생했습니다.");
        }
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
                .role(joinDTO.getRole())
                .build();
    }
}