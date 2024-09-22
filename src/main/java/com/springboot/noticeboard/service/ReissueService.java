package com.springboot.noticeboard.service;

import com.springboot.noticeboard.entity.RefreshEntity;
import com.springboot.noticeboard.jwt.JWTUtil;
import com.springboot.noticeboard.repository.RefreshRepository;
import io.jsonwebtoken.ExpiredJwtException;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.util.Date;

@Service
@RequiredArgsConstructor
public class ReissueService {

    private final JWTUtil jwtUtil;
    private final RefreshRepository refreshRepository;
    private final CookieService cookieService;

    public ResponseEntity<?> reissue(HttpServletRequest request, HttpServletResponse response) {
        String refresh = getRefreshTokenFromCookies(request);

        if (refresh == null) {
            return new ResponseEntity<>("refresh token null", HttpStatus.BAD_REQUEST);
        }

        try {
            jwtUtil.isExpired(refresh);
        } catch (ExpiredJwtException e) {
            return new ResponseEntity<>("refresh token expired", HttpStatus.BAD_REQUEST);
        }

        String category = jwtUtil.getCategory(refresh);
        if (!"refresh".equals(category)) {
            return new ResponseEntity<>("invalid refresh token", HttpStatus.BAD_REQUEST);
        }

        Boolean isExist = refreshRepository.existsByRefresh(refresh);
        if (!isExist) {
            return new ResponseEntity<>("invalid refresh token", HttpStatus.BAD_REQUEST);
        }

        String email = jwtUtil.getEmail(refresh);
        String role = jwtUtil.getRole(refresh);

        // 새로운 토큰 생성
        String newAccess = jwtUtil.createJwt("access", email, role, 600000L);
        String newRefresh = jwtUtil.createJwt("refresh", email, role, 86400000L);

        // DB에 새 Refresh 토큰 저장
        refreshRepository.deleteByRefresh(refresh);
        addRefreshEntity(email, newRefresh, 86400000L);

        // 응답에 토큰과 쿠키 추가
        response.setHeader("access", newAccess);
        response.addCookie(cookieService.createCookie("refresh", newRefresh));

        return new ResponseEntity<>(HttpStatus.OK);
    }

    private String getRefreshTokenFromCookies(HttpServletRequest request) {
        Cookie[] cookies = request.getCookies();
        if (cookies == null) return null;

        for (Cookie cookie : cookies) {
            if ("refresh".equals(cookie.getName())) {
                return cookie.getValue();
            }
        }
        return null;
    }

    private void addRefreshEntity(String email, String refresh, Long expiredMs) {
        Date expirationDate = new Date(System.currentTimeMillis() + expiredMs);

        RefreshEntity refreshEntity = RefreshEntity.builder()
                .email(email)
                .refresh(refresh)
                .expiration(expirationDate.toString())
                .build();

        refreshRepository.save(refreshEntity);
    }
}
