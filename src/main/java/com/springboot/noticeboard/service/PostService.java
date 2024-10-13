package com.springboot.noticeboard.service;

import com.querydsl.core.types.dsl.BooleanExpression;
import com.springboot.noticeboard.dto.request.PostDTO;
import com.springboot.noticeboard.dto.request.UpdatePostDTO;
import com.springboot.noticeboard.dto.response.ServiceResult;
import com.springboot.noticeboard.entity.PostEntity;
import com.springboot.noticeboard.entity.QPostEntity;
import com.springboot.noticeboard.entity.UserEntity;
import com.springboot.noticeboard.exception.BizException;
import com.springboot.noticeboard.repository.PostRepository;
import com.springboot.noticeboard.type.Role;
import jakarta.persistence.EntityManagerFactory;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class PostService {

    private final PostRepository postRepository;
    private final EntityManagerFactory entityManagerFactory;


    // 게시글 목록 조회 - Querydsl 적용
    public Page<PostEntity> getPosts(Pageable pageable, String keyword) {
        QPostEntity post = QPostEntity.postEntity;

        BooleanExpression expression = post.isNotNull(); // 기본적으로 모든 게시글을 가져오는 조건
        if (keyword != null && !keyword.isEmpty()) {
            expression = expression.and(post.title.containsIgnoreCase(keyword)
                    .or(post.content.containsIgnoreCase(keyword))); // 제목 또는 내용 검색
        }

        return postRepository.findAll(expression, pageable);  // Querydsl 사용
    }

    // 특정 게시글 조회
    public PostEntity getPost(Long postId) {
        return postRepository.findByIdWithAuthor(postId)
                .orElseThrow(() -> new BizException("게시물을 찾을 수 없습니다.", HttpStatus.NOT_FOUND));
    }

    // 게시글 생성
    public ServiceResult createPost(PostDTO postDTO, UserEntity currentUser) {

        try {
            postRepository.save(PostEntity.builder()
                    .title(postDTO.getTitle())
                    .content(postDTO.getContent())
                    .author(currentUser)
                    .build());
        } catch (Exception e) {
            // 예외 발생 시 500 상태 코드와 함께 메시지 전달
            throw new BizException("게시글 등록 중 오류가 발생했습니다.", HttpStatus.INTERNAL_SERVER_ERROR);
        }

        return ServiceResult.success(HttpStatus.CREATED, "게시글 등록 성공!");  // 성공 시 201 Created
    }

    // 게시글 수정
    @Transactional
    public ServiceResult updatePost(Long postId, UpdatePostDTO updatePostDTO, UserEntity currentUser) {

        PostEntity postEntity = postRepository.findById(postId)
                .orElseThrow(() -> new BizException("게시물을 찾을 수 없습니다.", HttpStatus.NOT_FOUND));

        // 게시글 작성자와 현재 사용자가 동일한지 확인
        if (!postEntity.getAuthor().getId().equals(currentUser.getId())) {
            throw new BizException("댓글을 수정할 권한이 없습니다.", HttpStatus.FORBIDDEN);
        }

        // Dirty Checking을 통해 변경된 필드 자동 감지
        postEntity.setTitle(updatePostDTO.getTitle());
        postEntity.setContent(updatePostDTO.getContent());

        // 여기서 `save()` 호출 없이도 변경사항이 자동으로 반영됨 (Dirty Checking)
        return ServiceResult.success(HttpStatus.OK, "게시글 수정 완료!");
    }

    // 게시글 삭제
    public ServiceResult deletePost(Long postId, UserEntity currentUser) {

        PostEntity postEntity = postRepository.findById(postId)
                .orElseThrow(() -> new BizException("게시물을 찾을 수 없습니다.", HttpStatus.NOT_FOUND));

        // 게시글 작성자와 현재 사용자가 동일한지 확인
        if (!postEntity.getAuthor().getId().equals(currentUser.getId()) && !currentUser.getRole().equals(Role.ROLE_ADMIN)) {
            throw new BizException("게시글을 삭제할 권한이 없습니다.", HttpStatus.FORBIDDEN);
        }

        postRepository.delete(postEntity);

        return ServiceResult.success(HttpStatus.OK, "게시글 삭제 성공!");
    }

}
