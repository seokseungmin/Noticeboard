package com.springboot.noticeboard.service;

import com.springboot.noticeboard.dto.request.PostDTO;
import com.springboot.noticeboard.dto.request.UpdatePostDTO;
import com.springboot.noticeboard.dto.response.ServiceResult;
import com.springboot.noticeboard.entity.PostEntity;
import com.springboot.noticeboard.entity.UserEntity;
import com.springboot.noticeboard.exception.BizException;
import com.springboot.noticeboard.repository.PostRepository;
import com.springboot.noticeboard.type.Role;
import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityManagerFactory;
import jakarta.persistence.EntityTransaction;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class PostService {

    private final PostRepository postRepository;
    private final EntityManagerFactory entityManagerFactory;

    public ServiceResult createPost(PostDTO postDTO, UserEntity currentUser) {

        try {
            postRepository.save(PostEntity.builder()
                    .title(postDTO.getTitle())
                    .content(postDTO.getContent())
                    .author(currentUser)
                    .build());
        } catch (Exception e) {
            throw new BizException(e.getMessage());
        }

        return ServiceResult.success("게시글 등록 성공!");
    }

    // JPA 더티 체크(Dirty Checking) 적용
    @Transactional
    public ServiceResult updatePost(Long postId, UpdatePostDTO updatePostDTO, UserEntity currentUser) {

        PostEntity postEntity = postRepository.findById(postId)
                .orElseThrow(() -> new BizException("게시물을 찾지 못했습니다!"));

        // 게시글 작성자와 현재 사용자가 동일한지 확인
        if (!postEntity.getAuthor().getId().equals(currentUser.getId())) {
            throw new BizException("게시글을 수정할 권한이 없습니다.");
        }

        // Dirty Checking을 통해 변경된 필드 자동 감지
        postEntity.setTitle(updatePostDTO.getTitle());
        postEntity.setContent(updatePostDTO.getContent());

        // 여기서 `save()` 호출 없이도 변경사항이 자동으로 반영됨 (Dirty Checking)
        return ServiceResult.success("게시글 수정 완료!");
    }

    // 게시글 삭제
    public ServiceResult deletePost(Long postId, UserEntity currentUser) {

        PostEntity postEntity = postRepository.findById(postId)
                .orElseThrow(() -> new BizException("게시물을 찾지 못했습니다!"));

        // 게시글 작성자와 현재 사용자가 동일한지 확인
        if (!postEntity.getAuthor().getId().equals(currentUser.getId()) || !currentUser.getRole().equals(Role.ROLE_ADMIN)) {
            throw new BizException("게시글을 삭제할 권한이 없습니다.");
        }

        postRepository.delete(postEntity);

        return ServiceResult.success("게시글 삭제 성공!");
    }

    // 게시글 목록 조회
    public Page<PostEntity> getPosts(Pageable pageable) {
        return postRepository.findAll(pageable);
    }

    // 특정 게시글 조회
    public PostEntity getPost(Long postId) {
        return postRepository.findById(postId)
                .orElseThrow(() -> new BizException("게시물을 찾을 수 없습니다."));
    }

}
