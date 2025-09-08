package com.example.shimpyo.domain.auth.jwt;

import com.example.shimpyo.domain.auth.service.RedisService;
import com.example.shimpyo.domain.common.UserDetailsImpl;
import com.example.shimpyo.domain.common.UserDetailsServiceImpl;
import com.example.shimpyo.global.exceptionType.CommonException;
import com.example.shimpyo.global.exceptionType.ExceptionType;
import io.jsonwebtoken.ExpiredJwtException;
import jakarta.servlet.*;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

import static com.example.shimpyo.global.exceptionType.TokenException.*;

@Component
@Slf4j
@RequiredArgsConstructor
public class JwtTokenFilter extends OncePerRequestFilter {

    private final RedisService redisService;
    private final UserDetailsServiceImpl userDetailsService;
    private final JwtTokenProvider tokenProvider;

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

                UserDetailsImpl userDetails = userDetailsService.loadUserByUserLoginId(
                        tokenProvider.getUserNameFromAccess(token));
                UsernamePasswordAuthenticationToken authToken =
                        new UsernamePasswordAuthenticationToken(userDetails, null, userDetails.getAuthorities());
                SecurityContextHolder.getContext().setAuthentication(authToken);
            } catch (IllegalArgumentException ex) {
                setErrorResponse(response, CommonException.ILLEGAL_ARGUMENT);
                return;
            } catch (ExpiredJwtException e) {
                setErrorResponse(response, EXPIRED_TOKEN);
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
