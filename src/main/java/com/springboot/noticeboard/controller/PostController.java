package com.springboot.noticeboard.controller;

import com.springboot.noticeboard.dto.request.CustomUserDetails;
import com.springboot.noticeboard.dto.request.PostDTO;
import com.springboot.noticeboard.dto.request.UpdatePostDTO;
import com.springboot.noticeboard.dto.response.PostDetailDTO;
import com.springboot.noticeboard.dto.response.ResponseResult;
import com.springboot.noticeboard.dto.response.ServiceResult;
import com.springboot.noticeboard.dto.response.getPostsDTO;
import com.springboot.noticeboard.entity.PostEntity;
import com.springboot.noticeboard.entity.UserEntity;
import com.springboot.noticeboard.exception.BizException;
import com.springboot.noticeboard.repository.UserRepository;
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
@RequestMapping("/posts")
@RequiredArgsConstructor
public class PostController {

    private final PostService postService;
    private final UserRepository userRepository;

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

}
