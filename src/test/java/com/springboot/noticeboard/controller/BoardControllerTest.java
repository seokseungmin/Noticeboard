package com.springboot.noticeboard.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.springboot.noticeboard.dto.request.LoginRequestDTO;
import com.springboot.noticeboard.dto.request.PostDTO;
import com.springboot.noticeboard.dto.request.UpdatePostDTO;
import com.springboot.noticeboard.dto.response.ServiceResult;
import com.springboot.noticeboard.entity.CommentEntity;
import com.springboot.noticeboard.entity.PostEntity;
import com.springboot.noticeboard.entity.UserEntity;
import com.springboot.noticeboard.repository.PostRepository;
import com.springboot.noticeboard.repository.UserRepository;
import com.springboot.noticeboard.service.CommentService;
import com.springboot.noticeboard.service.PostService;
import com.springboot.noticeboard.type.Role;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.transaction.annotation.Transactional;

import java.util.Arrays;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@AutoConfigureMockMvc
@SpringBootTest
@Transactional  // 각 테스트마다 트랜잭션을 시작하고, 테스트 종료 후 롤백
class BoardControllerTest {
    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private AuthenticationManager authenticationManager;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PostRepository postRepository;

    @MockBean
    private PostService postService;

    @MockBean
    private CommentService commentService;

    private String accessToken;
    private UserEntity savedUser;
    private PostEntity firstPost;
    private PostEntity secondPost;
    private CommentEntity savedComment;

    @BeforeEach
    public void setUp() throws Exception {
        // 회원가입 및 로그인 후 accessToken 저장
        setupUserAndLogin();

        // 게시글 및 댓글 저장
        setupPostAndComment();
    }

    private void setupUserAndLogin() throws Exception {
        // 회원 등록
        savedUser = userRepository.save(UserEntity.builder()
                .username("seok")
                .password(passwordEncoder.encode("1234"))
                .email("seok@gmail.com")
                .role(Role.ROLE_USER)
                .build());

        // 로그인 및 인증 절차
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken("seok@gmail.com", "1234")
        );

        // 로그인 요청 및 accessToken 추출
        LoginRequestDTO loginDTO = LoginRequestDTO.builder()
                .email("seok@gmail.com")
                .password("1234")
                .build();

        MvcResult loginResult = mockMvc.perform(post("/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(loginDTO)))
                .andExpect(status().isOk())
                .andReturn();

        accessToken = loginResult.getResponse().getHeader("access");
        assertThat(accessToken).isNotNull();
    }

    private void setupPostAndComment() {
        // 게시글 저장
        firstPost = postRepository.save(PostEntity.builder()
                .title("Initial Test Title")
                .content("Initial Test Content")
                .author(savedUser)
                .build());

        // 두 번째 게시글 저장
        secondPost = postRepository.save(PostEntity.builder()
                .title("Second Test Title")
                .content("Second Test Content")
                .author(savedUser)
                .build());

        // 댓글 저장
        savedComment = CommentEntity.builder()
                .content("Test Comment 1")
                .author(savedUser)
                .post(firstPost)
                .build();
    }

    @Test
    @DisplayName("게시글 등록")
    public void testCreatePost() throws Exception {
        PostDTO postDTO = PostDTO.builder()
                .title("Test Title")
                .content("Test Content")
                .build();

        Mockito.when(postService.createPost(any(PostDTO.class), any(UserEntity.class)))
                .thenReturn(ServiceResult.success("게시글 등록 성공!"));

        mockMvc.perform(post("/boards")
                        .contentType(MediaType.APPLICATION_JSON)
                        .header("access", accessToken)
                        .content(objectMapper.writeValueAsString(postDTO)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.header.result").value(true))
                .andExpect(jsonPath("$.header.message").value("게시글 등록 성공!"));
    }

    @Test
    @DisplayName("게시글 수정")
    public void testUpdatePost() throws Exception {
        UpdatePostDTO updatePostDTO = UpdatePostDTO.builder()
                .title("Updated Title")
                .content("Updated Content")
                .build();

        Mockito.when(postService.updatePost(anyLong(), any(UpdatePostDTO.class), any(UserEntity.class)))
                .thenReturn(ServiceResult.success("게시글 수정 성공!"));

        mockMvc.perform(put("/boards/" + firstPost.getId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .header("access", accessToken)
                        .content(objectMapper.writeValueAsString(updatePostDTO)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.header.result").value(true))
                .andExpect(jsonPath("$.header.message").value("게시글 수정 성공!"));
    }

    @Test
    @DisplayName("게시글 삭제")
    public void testDeletePost() throws Exception {
        Mockito.when(postService.deletePost(anyLong(), any(UserEntity.class)))
                .thenReturn(ServiceResult.success("게시글 삭제 성공!"));

        mockMvc.perform(delete("/boards/" + firstPost.getId())
                        .header("access", accessToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.header.result").value(true))
                .andExpect(jsonPath("$.header.message").value("게시글 삭제 성공!"));
    }

    @Test
    @DisplayName("특정 게시글 조회")
    public void testGetPost() throws Exception {
        Mockito.when(postService.getPost(anyLong())).thenReturn(firstPost);

        mockMvc.perform(get("/boards/" + firstPost.getId())
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.title").value(firstPost.getTitle()))
                .andExpect(jsonPath("$.content").value(firstPost.getContent()))
                .andExpect(jsonPath("$.author").value(savedUser.getUsername()));
    }

    @Test
    @DisplayName("게시글 목록 조회 테스트")
    public void testGetPosts() throws Exception {
        // 두 개의 게시글을 리스트로 생성
        List<PostEntity> posts = Arrays.asList(firstPost, secondPost);

        // Page 객체로 두 개의 게시글을 설정
        Page<PostEntity> postPage = new PageImpl<>(posts, PageRequest.of(0, 10), posts.size());

        // postService.getPosts() 호출 시 두 개의 게시글을 반환하도록 설정
        Mockito.when(postService.getPosts(any(Pageable.class), anyString())).thenReturn(postPage);

        // API 호출 및 응답 검증
        mockMvc.perform(get("/boards")
                        .param("page", "0")
                        .param("size", "10")
                        .param("sortBy", "createDate")
                        .param("direction", "desc")
                        .param("keyword", "")  // 검색어를 빈 문자열로 설정
                        .header("access", accessToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[0].title").value(firstPost.getTitle()))
                .andExpect(jsonPath("$.content[0].commentCount").value(firstPost.getCommentCount()))
                .andExpect(jsonPath("$.content[1].title").value(secondPost.getTitle()))
                .andExpect(jsonPath("$.content[1].commentCount").value(secondPost.getCommentCount()));
    }

    @Test
    @DisplayName("댓글 목록 조회 테스트")
    public void testGetCommentsByPostId() throws Exception {
        List<CommentEntity> comments = Arrays.asList(savedComment);
        Page<CommentEntity> commentPage = new PageImpl<>(comments, PageRequest.of(0, 10), comments.size());

        Mockito.when(commentService.getCommentsByPostId(anyLong(), any(Pageable.class))).thenReturn(commentPage);

        mockMvc.perform(get("/boards/" + firstPost.getId() + "/comments")
                        .param("page", "0")
                        .param("size", "10")
                        .header("access", accessToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[0].content").value(savedComment.getContent()))
                .andExpect(jsonPath("$.content[0].author").value(savedUser.getUsername()));
    }
}