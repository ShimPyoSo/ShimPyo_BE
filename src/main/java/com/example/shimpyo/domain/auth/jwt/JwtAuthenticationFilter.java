package com.example.shimpyo.domain.auth.jwt;

import com.example.shimpyo.domain.auth.dto.LoginResponseDto;
import com.example.shimpyo.domain.auth.dto.UserLoginDto;
import com.example.shimpyo.domain.auth.service.RedisService;
import com.example.shimpyo.domain.common.UserDetailsImpl;
import com.example.shimpyo.domain.common.UserDetailsServiceImpl;
import com.example.shimpyo.domain.user.entity.User;
import com.example.shimpyo.domain.utils.CookieUtils;
import com.example.shimpyo.global.BaseException;
import com.example.shimpyo.global.ErrorResponse;
import com.example.shimpyo.global.exceptionType.AuthException;
import com.example.shimpyo.global.exceptionType.ExceptionType;
import com.example.shimpyo.global.exceptionType.MemberException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import jakarta.servlet.FilterChain;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.InternalAuthenticationServiceException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.security.web.util.matcher.AntPathRequestMatcher;

import java.io.IOException;

public class JwtAuthenticationFilter extends UsernamePasswordAuthenticationFilter {

    private final JwtTokenProvider jwtProvider;
    private final CookieUtils cookieUtils;
    private final RedisService redisService;
    private final UserDetailsServiceImpl userDetailsService;
    private final PasswordEncoder passwordEncoder;

    @Value("${jwt.expiration}")
    private long expiration;

    @Value("${jwt.expirationALRT}")
    private long expirationALRT;

    @Value("${jwt.expirationRT}")
    private long expirationRT;

    public JwtAuthenticationFilter(JwtTokenProvider jwtProvider, CookieUtils cookieUtils,
                                   AuthenticationManager authenticationManager, RedisService redisService, UserDetailsServiceImpl userDetailsService, PasswordEncoder passwordEncoder) {
        this.jwtProvider = jwtProvider;
        this.cookieUtils = cookieUtils;
        this.redisService = redisService;
        this.userDetailsService = userDetailsService;
        this.passwordEncoder = passwordEncoder;

        setAuthenticationManager(authenticationManager);
        setRequiresAuthenticationRequestMatcher(new AntPathRequestMatcher("/api/user/auth/login", "POST"));
    }

    @Override
    public Authentication attemptAuthentication(HttpServletRequest request, HttpServletResponse response) {
        try {
            // DTO 파싱
            UserLoginDto dto = new ObjectMapper().readValue(request.getInputStream(), UserLoginDto.class);

            // 회원 확인
            UserDetailsImpl userDetails = (UserDetailsImpl) userDetailsService.loadUserByUsername(dto.getUsername());
            if (!passwordEncoder.matches(dto.getPassword(), userDetails.getPassword())) {
                setResponse(response, AuthException.MEMBER_INFO_NOT_MATCHED);
                return null;
            }

            // 토큰 생성
            return getAuthenticationManager().authenticate(
                    new UsernamePasswordAuthenticationToken(userDetails, dto.getPassword(), userDetails.getAuthorities())
            );

        } catch (UsernameNotFoundException e) {
            setResponse(response, MemberException.MEMBER_NOT_FOUND);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        return null;
    }

    @Override
    protected void successfulAuthentication(HttpServletRequest request, HttpServletResponse response,
                                            FilterChain chain, Authentication authResult) throws IOException {
        ObjectMapper objectMapper = new ObjectMapper();

        UserDetailsImpl userDetails = (UserDetailsImpl) authResult.getPrincipal();
        boolean isRememberMe = Boolean.parseBoolean(request.getParameter("isRememberMe"));

        String accessToken = jwtProvider.createAccessToken(userDetails.getUsername());
        String refreshToken = jwtProvider.createRefreshToken(userDetails.getUsername(), isRememberMe);
        long expiry = (isRememberMe ? expirationALRT : expirationRT) / 1000L;

        cookieUtils.addCookies(response,
                cookieUtils.buildAccessCookie(accessToken, expiration / 1000L),
                cookieUtils.buildRefreshCookie(refreshToken, expiry)
        );

        User user = userDetails.getUserAuth().getUser();
        redisService.saveRefreshToken(user.getUserAuth().getUserLoginId(), refreshToken);

        response.setContentType("application/json;charset=UTF-8");
        response.setStatus(HttpServletResponse.SC_OK);
        objectMapper.writeValue(response.getWriter(), LoginResponseDto.toDto(user));
    }

    @Override
    protected void unsuccessfulAuthentication(HttpServletRequest request,
                                              HttpServletResponse response,
                                              AuthenticationException failed) throws IOException {

        response.setContentType("application/json;charset=UTF-8");

        if (failed instanceof BadCredentialsException) {
            setResponse(response, AuthException.MEMBER_INFO_NOT_MATCHED);
        } else if (failed instanceof UsernameNotFoundException
                || failed.getCause() instanceof UsernameNotFoundException) {
            setResponse(response, MemberException.MEMBER_NOT_FOUND);
        } else {
            setResponse(response, AuthException.AUTHENTICATION_GET_FAILED);
        }
    }


    private void setResponse(HttpServletResponse response, ExceptionType exceptionType) {
        try {
            response.setStatus(exceptionType.httpStatus().value());
            response.setContentType("application/json;charset=UTF-8");
            response.getWriter().write(new ObjectMapper().writeValueAsString(ErrorResponse.of(exceptionType)));
        } catch (IOException ex) {
            throw new RuntimeException(ex);
        }
    }
}
