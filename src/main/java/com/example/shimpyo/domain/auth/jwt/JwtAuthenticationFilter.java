package com.example.shimpyo.domain.auth.jwt;

import com.example.shimpyo.domain.auth.dto.LoginResponseDto;
import com.example.shimpyo.domain.auth.dto.UserLoginDto;
import com.example.shimpyo.domain.auth.service.RedisService;
import com.example.shimpyo.domain.common.UserDetailsImpl;
import com.example.shimpyo.domain.user.entity.User;
import com.example.shimpyo.domain.utils.CookieUtils;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import jakarta.servlet.FilterChain;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.security.web.util.matcher.AntPathRequestMatcher;

import java.io.IOException;

@RequiredArgsConstructor
public class JwtAuthenticationFilter extends UsernamePasswordAuthenticationFilter {

    private final JwtTokenProvider jwtProvider;
    private final CookieUtils cookieUtils;
    private final RedisService redisService;

    @Value("${jwt.expiration}")
    long expiration;

    @Value("${jwt.expirationALRT}")
    long expirationALRT;

    @Value("${jwt.expirationRT}")
    long expirationRT;

    public JwtAuthenticationFilter(JwtTokenProvider jwtProvider, CookieUtils cookieUtils,
                                   AuthenticationManager authenticationManager, RedisService redisService) {
        this.jwtProvider = jwtProvider;
        this.cookieUtils = cookieUtils;
        this.redisService = redisService;

        setAuthenticationManager(authenticationManager);
        setRequiresAuthenticationRequestMatcher(new AntPathRequestMatcher("/api/user/auth/login", "POST"));
    }

    @Override
    public Authentication attemptAuthentication(HttpServletRequest request, HttpServletResponse response) {
        try {
            UserLoginDto requestDto = new ObjectMapper()
                    .readValue(request.getInputStream(), UserLoginDto.class);
            return getAuthenticationManager().authenticate(new UsernamePasswordAuthenticationToken(
                    requestDto.getUsername(),
                    requestDto.getPassword()
            ));
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    protected void successfulAuthentication(HttpServletRequest request, HttpServletResponse response,
                                            FilterChain chain, Authentication authResult) throws IOException {

        UserDetailsImpl userDetails = (UserDetailsImpl) authResult.getPrincipal();
        String rememberMeParam = request.getParameter("isRememberMe");
        boolean isRememberMe = false;
        if (rememberMeParam != null) {
            isRememberMe = Boolean.parseBoolean(rememberMeParam);
        }
        String accessToken = jwtProvider.createAccessToken(userDetails.getUsername());
        String refreshToken = jwtProvider.createRefreshToken(userDetails.getUsername(), isRememberMe);
        long expiry = (isRememberMe ? expirationALRT : expirationRT) / 1000L;

        cookieUtils.addCookies(response, cookieUtils.buildAccessCookie(accessToken, expiration / 1000L),
                cookieUtils.buildRefreshCookie(refreshToken, expiry));

        User user = userDetails.getUserAuth().getUser();
        redisService.saveRefreshToken(user.getUserAuth().getUserLoginId(), refreshToken);
        response.setContentType("application/json;charset=UTF-8");
        response.setStatus(HttpServletResponse.SC_OK);

        ObjectMapper objectMapper = new ObjectMapper();
        ObjectNode rootNode = objectMapper.valueToTree(LoginResponseDto.toDto(user));
        objectMapper.writeValue(response.getWriter(), rootNode);
    }

}
