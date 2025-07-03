package com.example.shimpyo.domain.user.controller;

import com.example.shimpyo.domain.user.dto.LoginResponseDto;
import com.example.shimpyo.domain.user.oauth.OAuth2Service;
import com.fasterxml.jackson.core.JsonProcessingException;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/user/auth")
@RequiredArgsConstructor
public class AuthController {

    private final OAuth2Service oAuth2Service;

    @PostMapping("/social/login")
    public ResponseEntity<LoginResponseDto> getKaKaoToken(@RequestBody Map<String, String> requestDto) throws JsonProcessingException {
        return ResponseEntity.ok(oAuth2Service.kakaoLogin(requestDto.get("accessToken")));
    }
}
