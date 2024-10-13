package com.springboot.noticeboard.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.springboot.noticeboard.dto.request.JoinDTO;
import com.springboot.noticeboard.dto.response.ServiceResult;
import com.springboot.noticeboard.service.JoinService;
import com.springboot.noticeboard.type.Role;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.ArgumentMatchers.any;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc  // MockMvc 자동 설정
public class JoinControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private JoinService joinService;

    @Autowired
    private ObjectMapper objectMapper;  // JSON 직렬화에 사용되는 ObjectMapper 추가

    @Test
    public void testJoinProcess() throws Exception {
        // given
        JoinDTO joinDTO = JoinDTO.builder()
                .username("seok")
                .password("1234")
                .email("seok@gmail.com")
                .role(Role.ROLE_USER)
                .build();

        // Mock JoinService의 응답 설정
        Mockito.when(joinService.joinProcess(any(JoinDTO.class)))
                .thenReturn(ServiceResult.success(HttpStatus.OK, "회원 가입에 성공했습니다!"));

        // JSON 변환을 위한 ObjectMapper 사용
        String jsonRequest = objectMapper.writeValueAsString(joinDTO);

        // when & then
        mockMvc.perform(post("/join")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jsonRequest)
                        .accept(MediaType.APPLICATION_JSON)  // JSON 응답을 기대함
                        .characterEncoding("UTF-8"))         // 인코딩을 UTF-8로 설정
                .andExpect(status().isOk())
                .andDo(result -> System.out.println(result.getResponse().getContentAsString()));  // 응답 본문 출력

    }
}
