package com.springboot.noticeboard.repository;

import com.springboot.noticeboard.entity.CommentEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface CommentRepository extends JpaRepository<CommentEntity, Long> {

    // 특정 게시글의 댓글을 최신순으로 페이징 처리하여 가져오기
    @Query("SELECT c FROM CommentEntity c JOIN FETCH c.author WHERE c.post.id = :postId ORDER BY c.createDate DESC")
    Page<CommentEntity> findByPostIdWithAuthor(@Param("postId") Long postId, Pageable pageable);

}

