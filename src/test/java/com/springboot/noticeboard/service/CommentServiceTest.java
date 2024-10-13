package com.springboot.noticeboard.service;

import com.springboot.noticeboard.dto.request.CommentDTO;
import com.springboot.noticeboard.dto.request.UpdateCommentDTO;
import com.springboot.noticeboard.entity.CommentEntity;
import com.springboot.noticeboard.entity.PostEntity;
import com.springboot.noticeboard.entity.UserEntity;
import com.springboot.noticeboard.exception.BizException;
import com.springboot.noticeboard.repository.CommentRepository;
import com.springboot.noticeboard.repository.PostRepository;
import com.springboot.noticeboard.type.Role;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;

@SpringBootTest
public class CommentServiceTest {

    @Autowired
    private CommentService commentService;

    @MockBean
    private CommentRepository commentRepository;

    @MockBean
    private PostRepository postRepository;

    private UserEntity mockUser;
    private PostEntity mockPost;
    private CommentEntity mockComment;

    @BeforeEach
    public void setup() {
        mockUser = UserEntity.builder()
                .id(1L)
                .username("testuser")
                .email("testuser@example.com")
                .password("password")
                .role(Role.ROLE_USER)
                .build();

        mockPost = PostEntity.builder()
                .id(1L)
                .title("Test Post")
                .content("Test Content")
                .author(mockUser)
                .build();

        mockComment = CommentEntity.builder()
                .id(1L)
                .content("This is a comment")
                .author(mockUser)
                .post(mockPost)
                .build();
    }

    @Test
    @DisplayName("댓글 작성 테스트")
    public void testAddComment() {
        // Given
        CommentDTO commentDTO = new CommentDTO();
        commentDTO.setContent("This is a test comment");

        Mockito.when(postRepository.findById(eq(1L))).thenReturn(Optional.of(mockPost));
        Mockito.when(commentRepository.save(any(CommentEntity.class))).thenReturn(mockComment);

        // When
        var result = commentService.addComment(1L, commentDTO, mockUser);

        // Then
        assertThat(result.isResult()).isTrue();
        assertThat(result.getMessage()).isEqualTo("댓글 작성이 완료되었습니다.");
    }

    @Test
    @DisplayName("댓글 수정 테스트")
    public void testUpdateComment() {
        // Given
        UpdateCommentDTO updateCommentDTO = new UpdateCommentDTO();
        updateCommentDTO.setContent("Updated comment");

        Mockito.when(commentRepository.findById(eq(1L))).thenReturn(Optional.of(mockComment));

        // When
        var result = commentService.updateComment(1L, updateCommentDTO, mockUser);

        // Then
        assertThat(result.isResult()).isTrue();
        assertThat(result.getMessage()).isEqualTo("댓글이 성공적으로 수정되었습니다.");
    }

    @Test
    @DisplayName("댓글 삭제 테스트")
    public void testDeleteComment() {
        // Given
        Mockito.when(commentRepository.findById(eq(1L))).thenReturn(Optional.of(mockComment));

        // When
        var result = commentService.deleteComment(1L, mockUser);

        // Then
        assertThat(result.isResult()).isTrue();
        assertThat(result.getMessage()).isEqualTo("댓글이 성공적으로 삭제되었습니다.");
    }

    @Test
    @DisplayName("댓글 삭제 권한 없음 테스트")
    public void testDeleteCommentWithoutPermission() {
        // Given
        UserEntity anotherUser = UserEntity.builder()
                .id(2L)
                .username("anotherUser")
                .email("anotherUser@example.com")
                .role(Role.ROLE_USER)
                .build();

        Mockito.when(commentRepository.findById(eq(1L))).thenReturn(Optional.of(mockComment));

        // When & Then
        assertThrows(BizException.class, () -> commentService.deleteComment(1L, anotherUser));
    }
}
