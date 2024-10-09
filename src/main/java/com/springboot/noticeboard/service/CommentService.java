package com.springboot.noticeboard.service;

import com.springboot.noticeboard.dto.request.CommentDTO;
import com.springboot.noticeboard.dto.response.ServiceResult;
import com.springboot.noticeboard.entity.CommentEntity;
import com.springboot.noticeboard.entity.PostEntity;
import com.springboot.noticeboard.entity.UserEntity;
import com.springboot.noticeboard.exception.BizException;
import com.springboot.noticeboard.repository.CommentRepository;
import com.springboot.noticeboard.repository.PostRepository;
import com.springboot.noticeboard.repository.UserRepository;
import com.springboot.noticeboard.type.Role;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class CommentService {

    private final CommentRepository commentRepository;
    private final PostRepository postRepository;
    private final UserRepository userRepository;

    @Transactional
    public ServiceResult addComment(CommentDTO commentDTO, UserEntity author) {

        // 게시글 존재 여부 확인
        PostEntity post = postRepository.findById(commentDTO.getPostId())
                .orElseThrow(() -> new BizException("게시글을 찾을 수 없습니다."));

        // 댓글 저장
        CommentEntity comment = CommentEntity.builder()
                .content(commentDTO.getContent())
                .author(author)
                .post(post)
                .build();

        // 게시글의 commentCount 증가
        post.setCommentCount(post.getCommentCount() + 1);
        postRepository.save(post);  // 변경 사항을 반영하기 위해 save

        commentRepository.save(comment);
        return ServiceResult.success("댓글 작성이 완료되었습니다.");
    }

    @Transactional
    public ServiceResult deleteComment(Long commentId, UserEntity currentUser) {

        // 댓글이 존재하는지 확인
        CommentEntity comment = commentRepository.findById(commentId)
                .orElseThrow(() -> new BizException("댓글을 찾을 수 없습니다."));

        // 댓글 주인이거나 관리자인지 확인
        if (!comment.getAuthor().getId().equals(currentUser.getId()) && !currentUser.getRole().equals(Role.ROLE_ADMIN)) {
            throw new BizException("댓글을 삭제할 권한이 없습니다.");
        }

        // 게시글의 commentCount 감소
        PostEntity post = comment.getPost();
        post.setCommentCount(post.getCommentCount() - 1);
        postRepository.save(post);  // 변경 사항을 반영하기 위해 save

        // 댓글 삭제
        commentRepository.delete(comment);
        return ServiceResult.success("댓글이 성공적으로 삭제되었습니다.");
    }

    // 댓글 목록 조회
    @Transactional(readOnly = true)
    public Page<CommentEntity> getCommentsByPostId(Long postId, Pageable pageable) {

        // 게시글이 존재하는지 확인 (존재하지 않으면 예외 발생)
        PostEntity post = postRepository.findById(postId)
                .orElseThrow(() -> new BizException("게시글을 찾을 수 없습니다."));

        // 특정 게시글의 댓글 목록을 페이징 처리하여 최신순으로 가져옴
        return commentRepository.findByPostIdOrderByCreateDateDesc(postId, pageable);
    }
}
