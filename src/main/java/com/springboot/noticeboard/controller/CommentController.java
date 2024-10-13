package com.springboot.noticeboard.controller;

import com.springboot.noticeboard.dto.request.CommentDTO;
import com.springboot.noticeboard.dto.request.CustomUserDetails;
import com.springboot.noticeboard.dto.request.UpdateCommentDTO;
import com.springboot.noticeboard.dto.response.ResponseResult;
import com.springboot.noticeboard.dto.response.ServiceResult;
import com.springboot.noticeboard.dto.response.getCommentsByPostIdDTO;
import com.springboot.noticeboard.entity.CommentEntity;
import com.springboot.noticeboard.entity.UserEntity;
import com.springboot.noticeboard.exception.BizException;
import com.springboot.noticeboard.repository.UserRepository;
import com.springboot.noticeboard.service.CommentService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/comments")
@RequiredArgsConstructor
public class CommentController {

    private final CommentService commentService;
    private final UserRepository userRepository;

    private UserEntity getCurrentUser(Authentication authentication) {
        if (authentication == null || !authentication.isAuthenticated()) {
            throw new BizException("인증 정보가 없습니다.", HttpStatus.UNAUTHORIZED);
        }

        CustomUserDetails customUserDetails = (CustomUserDetails) authentication.getPrincipal();
        return userRepository.findByEmail(customUserDetails.getUsername())
                .orElseThrow(() -> new BizException("사용자를 찾을 수 없습니다.", HttpStatus.NOT_FOUND));
    }

    // 댓글 작성
    @PostMapping("/{postId}")
    public ResponseEntity<?> addComment(@PathVariable Long postId, @RequestBody @Valid CommentDTO commentDTO, Authentication authentication) {
        UserEntity currentUser = getCurrentUser(authentication);
        ServiceResult result = commentService.addComment(postId, commentDTO, currentUser);
        return ResponseResult.result(result);
    }

    // 댓글 수정 API
    @PutMapping("/{commentId}")
    public ResponseEntity<?> updateComment(@PathVariable Long commentId,
                                           @RequestBody @Valid UpdateCommentDTO updateCommentDTO,
                                           Authentication authentication) {
        UserEntity currentUser = getCurrentUser(authentication);
        ServiceResult result = commentService.updateComment(commentId, updateCommentDTO, currentUser);
        return ResponseResult.result(result);
    }

    // 댓글 삭제 API
    @DeleteMapping("/{commentId}")
    public ResponseEntity<?> deleteComment(@PathVariable Long commentId, Authentication authentication) {
        UserEntity currentUser = getCurrentUser(authentication);
        ServiceResult result = commentService.deleteComment(commentId, currentUser);
        return ResponseResult.result(result);
    }

    // 댓글 목록 조회 API
    @GetMapping("/{postId}")
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
