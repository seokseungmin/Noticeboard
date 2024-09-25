package com.springboot.noticeboard.service;

import com.springboot.noticeboard.dto.request.PostDTO;
import com.springboot.noticeboard.dto.request.UpdatePostDTO;
import com.springboot.noticeboard.dto.response.ServiceResult;
import com.springboot.noticeboard.entity.PostEntity;
import com.springboot.noticeboard.entity.UserEntity;
import com.springboot.noticeboard.exception.BizException;
import com.springboot.noticeboard.repository.PostRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class PostService {

    private final PostRepository postRepository;

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

    public ServiceResult updatePost(Long postId, UpdatePostDTO updatePostDTO, UserEntity currentUser) {

        PostEntity postEntity = postRepository.findById(postId)
                .orElseThrow(() -> new BizException("게시물을 찾지 못했습니다!"));

        // 게시글 작성자와 현재 사용자가 동일한지 확인
        if (!postEntity.getAuthor().getId().equals(currentUser.getId())) {
            throw new BizException("게시글을 수정할 권한이 없습니다.");
        }

        postEntity.setTitle(updatePostDTO.getTitle());
        postEntity.setContent(updatePostDTO.getContent());

        postRepository.save(postEntity);

        return ServiceResult.success("게시글 수정 완료!");
    }

    // 게시글 삭제
    public ServiceResult deletePost(Long postId, UserEntity currentUser) {

        PostEntity postEntity = postRepository.findById(postId)
                .orElseThrow(() -> new BizException("게시물을 찾지 못했습니다!"));

        // 게시글 작성자와 현재 사용자가 동일한지 확인
        if (!postEntity.getAuthor().getId().equals(currentUser.getId())) {
            throw new BizException("게시글을 삭제할 권한이 없습니다.");
        }

        postRepository.delete(postEntity);

        return ServiceResult.success("게시글 삭제 성공!");
    }
}
