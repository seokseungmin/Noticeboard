package com.springboot.noticeboard.controller;

import com.springboot.noticeboard.dto.request.CustomUserDetails;
import com.springboot.noticeboard.dto.request.PostDTO;
import com.springboot.noticeboard.dto.request.UpdatePostDTO;
import com.springboot.noticeboard.dto.response.ResponseResult;
import com.springboot.noticeboard.dto.response.ServiceResult;
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
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.Errors;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/boards")
@RequiredArgsConstructor
public class BoardController {

    private final PostService postService;
    private final UserRepository userRepository;

    private UserEntity getCurrentUser(Authentication authentication) {
        if (authentication == null || !authentication.isAuthenticated()) {
            throw new BizException("인증 정보가 없습니다.");
        }

        CustomUserDetails customUserDetails = (CustomUserDetails) authentication.getPrincipal();
        return userRepository.findByEmail(customUserDetails.getUsername())
                .orElseThrow(() -> new BizException("사용자를 찾을 수 없습니다."));
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

    //SecurityConfig에 권한 없어도 접근할수 있게 해놓았는데도 403 에러가 계속뜸
    // 게시글 목록 조회
    @GetMapping()
    public ResponseEntity<?> getPosts(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "createDate") String sortBy,  // createDate로 변경
            @RequestParam(defaultValue = "desc") String direction) {

        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.fromString(direction), sortBy));
        Page<PostEntity> postPage = postService.getPosts(pageable);

        return ResponseEntity.ok(postPage.map(post -> Map.of(
                "id", post.getId(),
                "title", post.getTitle(),
                "createDate", post.getCreateDate(),  // 이 부분도 createDate로 수정
                "commentCount", post.getCommentCount()
        )));
    }

    @GetMapping("/{postId}")
    public ResponseEntity<?> getPost(@PathVariable Long postId) {
        PostEntity post = postService.getPost(postId);
        return ResponseEntity.ok(Map.of(
                "title", post.getTitle(),
                "content", post.getContent(),
                "author", post.getAuthor().getUsername(),
                "createDate", post.getCreateDate()
        ));
    }

}
