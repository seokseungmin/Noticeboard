package com.springboot.noticeboard.controller;

import com.springboot.noticeboard.dto.request.CustomUserDetails;
import com.springboot.noticeboard.dto.request.PostDTO;
import com.springboot.noticeboard.dto.request.UpdatePostDTO;
import com.springboot.noticeboard.dto.response.ErrorResponseHandler;
import com.springboot.noticeboard.dto.response.ResponseResult;
import com.springboot.noticeboard.dto.response.ServiceResult;
import com.springboot.noticeboard.entity.UserEntity;
import com.springboot.noticeboard.repository.UserRepository;
import com.springboot.noticeboard.service.PostService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.validation.Errors;
import org.springframework.web.bind.annotation.*;

import java.util.Optional;

@RestController
@RequestMapping("/board")
@RequiredArgsConstructor
public class PostController {

    private final PostService postService;
    private final UserRepository userRepository;

    // 게시글 작성
    @PostMapping("/createPost")
    public ResponseEntity<?> createPost(@RequestBody @Valid PostDTO postDTO, Errors errors, Authentication authentication) {

        // 에러 처리 부분을 별도 핸들러로 위임
        ResponseEntity<?> errorResponse = ErrorResponseHandler.handleValidationErrors(errors);
        if (errorResponse != null) {
            return errorResponse;
        }
        // CustomUserDetails에서 UserEntity 가져오기
        CustomUserDetails customUserDetails = (CustomUserDetails) authentication.getPrincipal();
        Optional<UserEntity> optionalUserEntity = userRepository.findByEmail(customUserDetails.getUsername());
        UserEntity currentUser = optionalUserEntity.get();
        ServiceResult serviceResult = postService.createPost(postDTO, currentUser);
        return ResponseResult.result(serviceResult);
    }

    // 게시글 수정
    @PutMapping("/updatePost/{postId}")
    public ResponseEntity<?> updatePost(@PathVariable Long postId,
                                        @RequestBody @Valid UpdatePostDTO updatePostDTO, Errors errors, Authentication authentication) {

        // 에러 처리 부분을 별도 핸들러로 위임
        ResponseEntity<?> errorResponse = ErrorResponseHandler.handleValidationErrors(errors);
        if (errorResponse != null) {
            return errorResponse;
        }
        CustomUserDetails customUserDetails = (CustomUserDetails) authentication.getPrincipal();
        Optional<UserEntity> optionalUserEntity = userRepository.findByEmail(customUserDetails.getUsername());
        UserEntity currentUser = optionalUserEntity.get();
        ServiceResult serviceResult = postService.updatePost(postId, updatePostDTO, currentUser);
        return ResponseResult.result(serviceResult);
    }

    // 게시글 삭제
    @DeleteMapping("/deletePost/{postId}")
    public ResponseEntity<?> deletePost(@PathVariable Long postId, Authentication authentication) {
        CustomUserDetails customUserDetails = (CustomUserDetails) authentication.getPrincipal();
        Optional<UserEntity> optionalUserEntity = userRepository.findByEmail(customUserDetails.getUsername());
        UserEntity currentUser = optionalUserEntity.get();
        ServiceResult serviceResult = postService.deletePost(postId, currentUser);
        return ResponseResult.result(serviceResult);
    }
}
