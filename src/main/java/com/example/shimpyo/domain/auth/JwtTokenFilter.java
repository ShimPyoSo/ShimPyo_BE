package com.example.shimpyo.domain.auth;

import com.example.shimpyo.domain.user.utils.RedisService;
import com.example.shimpyo.global.BaseException;
import com.example.shimpyo.global.exceptionType.CommonException;
import com.example.shimpyo.global.exceptionType.ExceptionType;
import com.example.shimpyo.global.exceptionType.TokenException;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import jakarta.servlet.*;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.security.Key;
import java.util.*;

import static com.example.shimpyo.global.exceptionType.TokenException.INVALID_TOKEN;
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

        if (token != null) {
            try {
                if(redisService.isBlackList(token)){
                    setErrorResponse(response, TOKEN_IS_BLACKLISTED);
                    return;
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

                Map<String, Object> principal = new HashMap<>();
                principal.put("loginId", loginId);
                principal.put("userId", userId);
                Authentication authentication = new UsernamePasswordAuthenticationToken(
                        principal, "", Collections.emptyList());

                SecurityContextHolder.getContext().setAuthentication(authentication);
            } catch (IllegalArgumentException ex) {
                setErrorResponse(response, CommonException.ILLEGAL_ARGUMENT);
                return;
            } catch (ExpiredJwtException e) {
                setErrorResponse(response, INVALID_TOKEN);
                return;
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

    private void setErrorResponse(HttpServletResponse response, ExceptionType code) throws IOException {
        log.error(code.message());
        response.setStatus(HttpStatus.UNAUTHORIZED.value());
        response.setContentType("application/json;charset=UTF-8");
        response.getWriter().write(String.format("{\"message\": \"%s\"}", code.message()));
    }
}
