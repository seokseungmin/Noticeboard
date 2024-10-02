package com.springboot.noticeboard.jwt;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.springboot.noticeboard.dto.request.LoginRequestDTO;
import com.springboot.noticeboard.entity.RefreshEntity;
import com.springboot.noticeboard.repository.RefreshRepository;
import com.springboot.noticeboard.service.CookieService;
import jakarta.servlet.FilterChain;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

import java.io.IOException;
import java.util.Collection;
import java.util.Date;
import java.util.Iterator;

// Logger 적용으로 로깅 개선
@Slf4j
@RequiredArgsConstructor
public class LoginFilter extends UsernamePasswordAuthenticationFilter {

    private final AuthenticationManager authenticationManager;
    //JWTUtil 주입
    private final JWTUtil jwtUtil;
    private final RefreshRepository refreshRepository;
    private final CookieService cookieService;

    @Override
    public Authentication attemptAuthentication(HttpServletRequest request, HttpServletResponse response) throws AuthenticationException {

        // JSON 형태로 받은 요청에서 email password를 추출하는 방법입니다.
        // JSON 데이터를 처리할 DTO 클래스
        LoginRequestDTO loginRequest = null;
        try {
            // JSON 데이터를 LoginRequest DTO로 변환
            loginRequest = new ObjectMapper().readValue(request.getInputStream(), LoginRequestDTO.class);
            log.debug("Login attempt for user with email: {}", loginRequest.getEmail());
        } catch (IOException e) {
            log.error("Failed to parse login request", e);
            throw new RuntimeException(e);
        }

        // 추출된 username과 password 사용
        String email = loginRequest.getEmail();
        String password = loginRequest.getPassword();

        log.debug("Attempting to authenticate user with email: {}", email);


        //스프링 시큐리티에서 email과 password를 검증하기 위해서는 token에 담아야 함
        UsernamePasswordAuthenticationToken authToken = new UsernamePasswordAuthenticationToken(email, password, null);
        log.debug("Generated authentication token for user: {}", email);

        //token에 담은 검증을 위한 AuthenticationManager로 전달
        return authenticationManager.authenticate(authToken);
    }

    //로그인 성공시 실행하는 메소드 (여기서 JWT를 발급하면 됨)
    @Override
    protected void successfulAuthentication(HttpServletRequest request, HttpServletResponse response, FilterChain chain, Authentication authentication) {
        //유저 정보
        String email = authentication.getName();
        log.info("Authentication successful for user: {}", email);

        Collection<? extends GrantedAuthority> authorities = authentication.getAuthorities();
        Iterator<? extends GrantedAuthority> iterator = authorities.iterator();
        GrantedAuthority auth = iterator.next();
        String role = auth.getAuthority();

        //토큰 생성
        String access = jwtUtil.createJwt("access", email, role, 600000L);
        String refresh = jwtUtil.createJwt("refresh", email, role, 86400000L);
        log.info("Generated access and refresh tokens for user: {}", email);

        //Refresh 토큰 저장
        addRefreshEntity(email, refresh, 86400000L);

        //응답 설정
        response.setHeader("access", access);
        response.addCookie(cookieService.createCookie("refresh", refresh));
        response.setStatus(HttpStatus.OK.value());
        log.debug("Access and refresh tokens sent in response for user: {}", email);
    }

    private void addRefreshEntity(String email, String refresh, Long expiredMs) {

        // 만료일자
        Date date = new Date(System.currentTimeMillis() + expiredMs);

        RefreshEntity refreshEntity = RefreshEntity.builder()
                .email(email)
                .refresh(refresh)
                .expiration(date.toString())
                .build();

        refreshRepository.save(refreshEntity);
    }

    //로그인 실패시 실행하는 메소드
    @Override
    protected void unsuccessfulAuthentication(HttpServletRequest request, HttpServletResponse response, AuthenticationException failed) {
        log.warn("Authentication failed for user: {}", request.getParameter("email"), failed);
        //로그인 실패시 401 응답 코드 반환
        response.setStatus(401);
    }
}
