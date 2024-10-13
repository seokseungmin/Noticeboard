package com.springboot.noticeboard.controller;

import com.springboot.noticeboard.dto.request.CustomUserDetails;
import com.springboot.noticeboard.dto.request.PostDTO;
import com.springboot.noticeboard.dto.request.UpdatePostDTO;
import com.springboot.noticeboard.dto.response.*;
import com.springboot.noticeboard.entity.CommentEntity;
import com.springboot.noticeboard.entity.PostEntity;
import com.springboot.noticeboard.entity.UserEntity;
import com.springboot.noticeboard.exception.BizException;
import com.springboot.noticeboard.repository.UserRepository;
import com.springboot.noticeboard.service.CommentService;
import com.springboot.noticeboard.service.PostService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

// RESTful API 설계 개선
// 예외 처리 리팩토링
@RestController
@RequestMapping("/boards")
@RequiredArgsConstructor
public class BoardController {

    private final PostService postService;
    private final UserRepository userRepository;
    private final CommentService commentService;

    private UserEntity getCurrentUser(Authentication authentication) {
        if (authentication == null || !authentication.isAuthenticated()) {
            throw new BizException("인증 정보가 없습니다.", HttpStatus.UNAUTHORIZED);
        }

        CustomUserDetails customUserDetails = (CustomUserDetails) authentication.getPrincipal();
        return userRepository.findByEmail(customUserDetails.getUsername())
                .orElseThrow(() -> new BizException("사용자를 찾을 수 없습니다.", HttpStatus.NOT_FOUND));
    }

    // 게시글 작성
    @PostMapping()
    public ResponseEntity<?> createPost(@RequestBody @Valid PostDTO postDTO, Authentication authentication) {
        UserEntity currentUser = getCurrentUser(authentication);
        ServiceResult serviceResult = postService.createPost(postDTO, currentUser);
        return ResponseResult.result(serviceResult);
    }

    // 게시글 수정
    @Transactional
    @PutMapping("/{postId}")
    public ResponseEntity<?> updatePost(@PathVariable Long postId,
                                        @RequestBody @Valid UpdatePostDTO updatePostDTO, Authentication authentication) {
        UserEntity currentUser = getCurrentUser(authentication);
        ServiceResult serviceResult = postService.updatePost(postId, updatePostDTO, currentUser);
        return ResponseResult.result(serviceResult);
    }

    // 게시글 삭제
    @DeleteMapping("/{postId}")
    public ResponseEntity<?> deletePost(@PathVariable Long postId, Authentication authentication) {
        UserEntity currentUser = getCurrentUser(authentication);
        ServiceResult serviceResult = postService.deletePost(postId, currentUser);
        return ResponseResult.result(serviceResult);
    }

    // 게시글 목록 조회 (검색 기능 추가)
    @GetMapping()
    public ResponseEntity<?> getPosts(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "createDate") String sortBy,
            @RequestParam(defaultValue = "desc") String direction,
            @RequestParam(required = false) String keyword  // 검색어 추가
    ) {
        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.fromString(direction), sortBy));
        Page<PostEntity> postPage = postService.getPosts(pageable, keyword);  // 검색어 전달

        // PostDTO로 변환하여 반환
        Page<getPostsDTO> getPostsDTOS = postPage.map(post -> getPostsDTO.builder()
                .id(post.getId())
                .title(post.getTitle())
                .createDate(post.getCreateDate())
                .commentCount(post.getCommentCount())
                .build());

        return ResponseEntity.ok(getPostsDTOS);
    }

    @GetMapping("/{postId}")
    public ResponseEntity<?> getPost(@PathVariable Long postId) {
        PostEntity post = postService.getPost(postId);

        // PostDetailDTO로 변환하여 반환
        PostDetailDTO postDetailDTO = PostDetailDTO.builder()
                .title(post.getTitle())
                .content(post.getContent())
                .author(post.getAuthor().getUsername())
                .createDate(post.getCreateDate())
                .build();

        return ResponseEntity.ok(postDetailDTO);
    }

    // 댓글 목록 조회 API
    @GetMapping("{postId}/comments")
    public ResponseEntity<?> getCommentsByPostId(
            @PathVariable Long postId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {

        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "createDate"));
        Page<CommentEntity> commentPage = commentService.getCommentsByPostId(postId, pageable);

        // 댓글 목록을 CommentDTO로 변환하여 응답
        Page<getCommentsByPostIdDTO> commentDTOPage = commentPage.map(comment -> getCommentsByPostIdDTO.builder()
                .id(comment.getId())
                .content(comment.getContent())
                .author(comment.getAuthor().getUsername())  // 댓글 작성자
                .createDate(comment.getCreateDate())        // 댓글 작성일
                .build());

        return ResponseEntity.ok(commentDTOPage);
    }

}
