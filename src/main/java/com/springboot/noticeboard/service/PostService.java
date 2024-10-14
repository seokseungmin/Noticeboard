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
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Slf4j
@RequiredArgsConstructor
public class PostService {

    private final PostRepository postRepository;

    // 게시글 목록 조회 - Querydsl 적용
    public Page<PostEntity> getPosts(Pageable pageable, String keyword) {
        QPostEntity post = QPostEntity.postEntity;

        BooleanExpression expression = post.isNotNull(); // 기본적으로 모든 게시글을 가져오는 조건
        if (keyword != null && !keyword.isEmpty()) {
            expression = expression.and(post.title.contains(keyword)
                    .or(post.content.contains(keyword))); // 제목 또는 내용 검색
            log.info("Searching posts with keyword: {}", keyword);
        } else {
            log.info("Fetching all posts without search keyword");
        }

        return postRepository.findAll(expression, pageable);  // Querydsl 사용
    }

    // 특정 게시글 조회
    public PostEntity getPost(Long postId) {
        log.info("Fetching post with ID: {}", postId);

        return postRepository.findByIdWithAuthor(postId)
                .orElseThrow(() -> {
                    log.warn("Post with ID {} not found", postId);
                    return new BizException("게시물을 찾을 수 없습니다.", HttpStatus.NOT_FOUND);
                });
    }

    // 게시글 생성
    public ServiceResult createPost(PostDTO postDTO, UserEntity currentUser) {
        log.info("Creating new post by user: {}", currentUser.getEmail());

        // 예외 처리를 하지 않고 비즈니스 로직을 진행
        PostEntity post = PostEntity.builder()
                .title(postDTO.getTitle())
                .content(postDTO.getContent())
                .build();

        // 연관관계 편의 메서드를 통해 게시글 작성자 설정
        currentUser.addPost(post);

        // Repository에서 발생하는 예외는 GlobalExceptionHandler가 처리
        postRepository.save(post);
        log.info("Post created successfully with ID: {}", post.getId());


        return ServiceResult.success(HttpStatus.CREATED, "게시글 등록 성공!");  // 성공 시 201 Created
    }

    // 게시글 수정
    @Transactional
    public ServiceResult updatePost(Long postId, UpdatePostDTO updatePostDTO, UserEntity currentUser) {
        log.info("Updating post with ID: {} by user: {}", postId, currentUser.getEmail());

        PostEntity postEntity = postRepository.findById(postId)
                .orElseThrow(() -> {
                    log.warn("Post with ID {} not found", postId);
                    return new BizException("게시물을 찾을 수 없습니다.", HttpStatus.NOT_FOUND);
                });

        // 게시글 작성자와 현재 사용자가 동일한지 확인
        if (!postEntity.getAuthor().getId().equals(currentUser.getId())) {
            log.warn("User {} does not have permission to update post {}", currentUser.getEmail(), postId);
            throw new BizException("게시글을 수정할 권한이 없습니다.", HttpStatus.FORBIDDEN);
        }

        // Dirty Checking을 통해 변경된 필드 자동 감지
        postEntity.setTitle(updatePostDTO.getTitle());
        postEntity.setContent(updatePostDTO.getContent());

        // 여기서 `save()` 호출 없이도 변경사항이 자동으로 반영됨 (Dirty Checking)
        log.info("Post with ID: {} updated successfully", postId);
        return ServiceResult.success(HttpStatus.OK, "게시글 수정 완료!");
    }

    // 게시글 삭제
    public ServiceResult deletePost(Long postId, UserEntity currentUser) {
        log.info("Deleting post with ID: {} by user: {}", postId, currentUser.getEmail());

        PostEntity postEntity = postRepository.findById(postId)
                .orElseThrow(() -> {
                    log.warn("Post with ID {} not found", postId);
                    return new BizException("게시물을 찾을 수 없습니다.", HttpStatus.NOT_FOUND);
                });

        // 게시글 작성자와 현재 사용자가 동일한지 확인
        if (!postEntity.getAuthor().getId().equals(currentUser.getId()) && !currentUser.getRole().equals(Role.ROLE_ADMIN)) {
            log.warn("User {} does not have permission to delete post {}", currentUser.getEmail(), postId);
            throw new BizException("게시글을 삭제할 권한이 없습니다.", HttpStatus.FORBIDDEN);
        }

        // 연관관계 편의 메서드를 통해 게시글 삭제 처리
        currentUser.removePost(postEntity);

        postRepository.delete(postEntity);
        log.info("Post with ID: {} deleted successfully", postId);

        return ServiceResult.success(HttpStatus.OK, "게시글 삭제 성공!");
    }

}
