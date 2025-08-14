package com.example.shimpyo.domain.auth;

import com.example.shimpyo.domain.user.utils.RedisService;
import com.example.shimpyo.global.BaseException;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import jakarta.servlet.*;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.security.Key;
import java.util.*;

import static com.example.shimpyo.global.exceptionType.TokenException.TOKEN_IS_BLACKLISTED;

@Component
@Slf4j
@RequiredArgsConstructor
public class JwtTokenFilter extends OncePerRequestFilter {

    @Value("${jwt.secretKey}")
    private String secretKey;

    private final RedisService redisService;

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {

        String token = extractAccessToken(request);
        String refreshToken = extractRefreshToken(request);

        if (token != null || refreshToken != null) {
            try {
                // 로그아웃 되는 시점 토큰 탈취해 접근 권한 취득을 방지하는 코드
                if(!request.getRequestURI().equals("/api/user/auth/logout")
                        && redisService.isBlackList(token)){
                    throw new BaseException(TOKEN_IS_BLACKLISTED);
                }
                Key key = Keys.hmacShaKeyFor(secretKey.getBytes(StandardCharsets.UTF_8));
                Claims claims = Jwts.parserBuilder()
                        .setSigningKey(key)
                        .build()
                        .parseClaimsJws(token)
                        .getBody();

                // 이건 로그인 ID
                String loginId = claims.getSubject();
                // 이건 pk 값
                Long userId = claims.get("id", Number.class).longValue();

//                List<GrantedAuthority> authorities = new ArrayList<>();
//
//                authorities.add(new SimpleGrantedAuthority("ID_" + userId));
//                UserDetails userDetails = new User(loginId, "", authorities);
                Map<String, Object> principal = new HashMap<>();
                principal.put("loginId", loginId);
                principal.put("userId", userId);
                Authentication authentication = new UsernamePasswordAuthenticationToken(
                        principal, "", Collections.emptyList());

                SecurityContextHolder.getContext().setAuthentication(authentication);
            } catch (Exception e) {
                log.error("JWT 인증 실패: {}", e.getMessage());
                // 예외를 터뜨릴 수도 있고 무시할 수도 있음 (401 처리 여부에 따라)
            }
        }
        filterChain.doFilter(request, response);
    }

    private String extractAccessToken(HttpServletRequest request) {
        if (request.getCookies() == null) return null;
        for (Cookie cookie : request.getCookies()) {
            if ("access_token".equals(cookie.getName())) {
                return cookie.getValue();
            }
        }
        return null;
    }

    private String extractRefreshToken(HttpServletRequest request) {
        if (request.getCookies() == null) return null;
        for (Cookie cookie : request.getCookies()) {
            if ("refresh_token".equals(cookie.getName())) {
                return cookie.getValue();
            }
        }
        return null;
    }
}
