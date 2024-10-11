package com.springboot.noticeboard.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.springboot.noticeboard.dto.request.CommentDTO;
import com.springboot.noticeboard.dto.request.CustomUserDetails;
import com.springboot.noticeboard.dto.request.UpdateCommentDTO;
import com.springboot.noticeboard.dto.response.ServiceResult;
import com.springboot.noticeboard.entity.CommentEntity;
import com.springboot.noticeboard.entity.PostEntity;
import com.springboot.noticeboard.entity.UserEntity;
import com.springboot.noticeboard.repository.PostRepository;
import com.springboot.noticeboard.repository.UserRepository;
import com.springboot.noticeboard.service.CommentService;
import com.springboot.noticeboard.type.Role;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
public class CommentControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private CommentService commentService;

    @MockBean
    private PostRepository postRepository;

    @MockBean
    private UserRepository userRepository;

    private UserEntity currentUser;
    private PostEntity post;
    private CommentEntity comment;

    @BeforeEach
    void setUp() {
        // Mock 사용자 설정
        currentUser = UserEntity.builder()
                .id(1L)
                .username("seok")
                .email("seok@gmail.com")
                .password("password")
                .role(Role.ROLE_USER)
                .build();

        // Mock 게시글 설정
        post = PostEntity.builder()
                .id(1L)
                .title("Test Title")
                .content("Test Content")
                .author(currentUser)
                .build();

        // Mock 댓글 설정
        comment = CommentEntity.builder()
                .id(1L)
                .content("Test Comment")
                .author(currentUser)
                .post(post)
                .build();

        // Mock UserRepository와 PostRepository에서 사용자와 게시글을 찾아올 수 있도록 설정
        Mockito.when(userRepository.findByEmail(currentUser.getEmail()))
                .thenReturn(java.util.Optional.of(currentUser));
        Mockito.when(postRepository.findById(post.getId()))
                .thenReturn(java.util.Optional.of(post));
    }

    @Test
    @DisplayName("댓글 작성 테스트")
    public void testAddComment() throws Exception {
        // Given: 댓글 작성용 DTO 준비
        CommentDTO commentDTO = new CommentDTO("This is a test comment", post.getId());

        // When: 댓글 작성 서비스 호출을 Mocking
        Mockito.when(commentService.addComment(any(CommentDTO.class), any(UserEntity.class)))
                .thenReturn(ServiceResult.success("댓글 작성이 완료되었습니다."));

        // Then: MockMvc를 사용하여 API 호출 및 검증
        mockMvc.perform(post("/comments")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(commentDTO))
                        .with(SecurityMockMvcRequestPostProcessors.user(new CustomUserDetails(currentUser))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.header.result").value(true))
                .andExpect(jsonPath("$.header.message").value("댓글 작성이 완료되었습니다."));
    }

    @Test
    @DisplayName("댓글 수정 테스트")
    public void testUpdateComment() throws Exception {
        // Given: 댓글 수정용 DTO 준비
        UpdateCommentDTO updateCommentDTO = new UpdateCommentDTO("Updated comment");

        // When: 댓글 수정 서비스 호출을 Mocking
        Mockito.when(commentService.updateComment(anyLong(), any(UpdateCommentDTO.class), any(UserEntity.class)))
                .thenReturn(ServiceResult.success("댓글이 성공적으로 수정되었습니다."));

        // Then: MockMvc를 사용하여 API 호출 및 검증
        mockMvc.perform(put("/comments/" + comment.getId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updateCommentDTO))
                        .with(SecurityMockMvcRequestPostProcessors.user(new CustomUserDetails(currentUser))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.header.result").value(true))
                .andExpect(jsonPath("$.header.message").value("댓글이 성공적으로 수정되었습니다."));
    }

    @Test
    @DisplayName("댓글 삭제 테스트")
    public void testDeleteComment() throws Exception {
        // When: 댓글 삭제 서비스 호출을 Mocking
        Mockito.when(commentService.deleteComment(anyLong(), any(UserEntity.class)))
                .thenReturn(ServiceResult.success("댓글이 성공적으로 삭제되었습니다."));

        // Then: MockMvc를 사용하여 API 호출 및 검증
        mockMvc.perform(delete("/comments/" + comment.getId())
                        .with(SecurityMockMvcRequestPostProcessors.user(new CustomUserDetails(currentUser))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.header.result").value(true))
                .andExpect(jsonPath("$.header.message").value("댓글이 성공적으로 삭제되었습니다."));
    }

}
