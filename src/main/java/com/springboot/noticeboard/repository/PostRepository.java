package com.springboot.noticeboard.repository;

import com.springboot.noticeboard.entity.PostEntity;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PostRepository extends JpaRepository<PostEntity, Long> {
}
