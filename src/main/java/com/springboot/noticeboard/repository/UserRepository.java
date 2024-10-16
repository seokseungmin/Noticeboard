package com.springboot.noticeboard.repository;

import com.springboot.noticeboard.entity.UserEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface UserRepository extends JpaRepository<UserEntity, Long> {
    Optional<UserEntity> findByEmail(String email);
    boolean existsByEmail(String email);  // 이메일 중복 여부 확인
}
