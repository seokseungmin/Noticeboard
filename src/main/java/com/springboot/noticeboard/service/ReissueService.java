package com.springboot.noticeboard.service;

import com.springboot.noticeboard.dto.response.ServiceResult;
import com.springboot.noticeboard.entity.RefreshEntity;
import com.springboot.noticeboard.jwt.JWTUtil;
import com.springboot.noticeboard.repository.RefreshRepository;
import io.jsonwebtoken.ExpiredJwtException;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.Date;

@Service
@RequiredArgsConstructor
public class ReissueService {

    private final JWTUtil jwtUtil;
    private final RefreshRepository refreshRepository;
    private final CookieService cookieService;

    public ServiceResult reissue(HttpServletRequest request, HttpServletResponse response) {
        String refresh = getRefreshTokenFromCookies(request);

        if (refresh == null) {
            return ServiceResult.fail("refresh 토큰이 없습니다!");
        }

        try {
            jwtUtil.isExpired(refresh);
        } catch (ExpiredJwtException e) {
            return ServiceResult.fail("만료된 refresh 토큰!");
        }

        String category = jwtUtil.getCategory(refresh);
        if (!"refresh".equals(category)) {
            return ServiceResult.fail("refresh 토큰이 아닙니다!");
        }

        Boolean isExist = refreshRepository.existsByRefresh(refresh);
        if (!isExist) {
            return ServiceResult.fail("존재하지 않는 refresh 토큰입니다!");
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

        return ServiceResult.success("Access 토근, Refresh 토큰 재발급 성공!");
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
