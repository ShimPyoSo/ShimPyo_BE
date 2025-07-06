package com.example.shimpyo.domain.user.controller;

import com.example.shimpyo.domain.auth.JwtTokenProvider;
import com.example.shimpyo.domain.auth.dto.UserLoginDto;
import com.example.shimpyo.domain.user.dto.LoginResponseDto;
import com.example.shimpyo.domain.user.entity.UserAuth;
import com.example.shimpyo.domain.user.oauth.OAuth2Service;
import com.example.shimpyo.domain.user.service.AuthService;
import com.fasterxml.jackson.core.JsonProcessingException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseCookie;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/user/auth")
@RequiredArgsConstructor
public class AuthController {

    private final OAuth2Service oAuth2Service;
    private final AuthService  authService;

    @PostMapping("/social/login")
    public ResponseEntity<LoginResponseDto> getKaKaoToken(@RequestBody Map<String, String> requestDto) throws JsonProcessingException {
        return ResponseEntity.ok(oAuth2Service.kakaoLogin(requestDto.get("accessToken")));
    }

    // [#MOO3] 유저 로그인 시작
    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody UserLoginDto dto,
                                   HttpServletResponse response) throws JsonProcessingException {
        LoginResponseDto loginResponseDto = authService.login(dto, response);

        return ResponseEntity.ok(loginResponseDto);
    }
    // [#MOO3] 유저 로그인 끝

    // [#MOO6] AccessToken 재발급 로직
    @PostMapping("/refresh")
    public ResponseEntity<?> refreshToken(HttpServletRequest request, HttpServletResponse response) throws JsonProcessingException {
        authService.refreshToken(request, response);

        return ResponseEntity.ok("AccessToken 재발급 완료");
    }
    // [#MOO6] AccessToken 재발급 로직
}
