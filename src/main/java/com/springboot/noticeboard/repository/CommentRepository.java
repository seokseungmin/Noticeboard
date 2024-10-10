package com.springboot.noticeboard.repository;

import com.springboot.noticeboard.entity.CommentEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface CommentRepository extends JpaRepository<CommentEntity, Long> {

    // 특정 게시글의 댓글을 최신순으로 페이징 처리하여 가져오기
    Page<CommentEntity> findByPostIdOrderByCreateDateDesc(Long postId, Pageable pageable);
}

