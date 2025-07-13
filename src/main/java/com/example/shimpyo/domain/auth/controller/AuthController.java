package com.example.shimpyo.domain.auth.controller;

import com.example.shimpyo.domain.auth.dto.MailVerifyDto;
import com.example.shimpyo.domain.auth.dto.UserLoginDto;
import com.example.shimpyo.domain.auth.service.MailService;
import com.example.shimpyo.domain.auth.dto.LoginResponseDto;
import com.example.shimpyo.domain.auth.dto.MailCodeSendDto;
import com.example.shimpyo.domain.auth.dto.RegisterUserRequest;
import com.example.shimpyo.domain.auth.service.OAuth2Service;
import com.example.shimpyo.domain.auth.service.AuthService;
import com.fasterxml.jackson.core.JsonProcessingException;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/user/auth")
@RequiredArgsConstructor
public class AuthController {

    private final OAuth2Service oAuth2Service;
    private final AuthService  authService;
    private final MailService mailService;

    @PostMapping("/social/login")
    public ResponseEntity<LoginResponseDto> getKaKaoToken(@RequestBody Map<String, String> requestDto) throws JsonProcessingException {
        return ResponseEntity.ok(oAuth2Service.kakaoLogin(requestDto.get("accessToken")));
    }

    // [#MOO3] 유저 로그인 시작
    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody UserLoginDto dto, HttpServletResponse response) throws JsonProcessingException {
        LoginResponseDto loginResponseDto = authService.login(dto, response);

        return ResponseEntity.ok(loginResponseDto);
    }
    // [#MOO3] 유저 로그인 끝

    // [#MOO1] 사용자 회원가입 시작
    @PostMapping("/signup")
    public ResponseEntity<?> registerUser(@RequestBody RegisterUserRequest dto){
        authService.registerUser(dto);
        return ResponseEntity.ok("회원가입 완료");
    }
    // [#MOO1] 사용자 회원가입 끝N

    // [#M002] 이메일 검증 시작
    @GetMapping("/check/email/duplicate")
    public ResponseEntity<?> getEmail(@RequestParam String email){
        return ResponseEntity.ok(authService.emailCheck(email));
    }
    // [#M002] 이메일 검증 끝

    // [#MOO4] 이메일 인증 코드 발급 시작
    @PostMapping("/email")
    public ResponseEntity<?> sendEmail(@RequestBody MailCodeSendDto dto){
        mailService.authEmail(dto);

        return ResponseEntity.ok("이메일을 전송하였습니다.");
    }
    // [#MOO4] 이메일 인증 코드 발급 끝

    // [#MOO5] 이메일 인증 코드 검증 시작
    @PostMapping("/email/verify")
    public ResponseEntity<?> verifyEmail(@RequestBody MailVerifyDto dto){
        mailService.verifyAuthCode(dto);

        return ResponseEntity.ok("인증 완료");
    }
    // [#MOO5] 이메일 인증 코드 검증 끝

    // 아이디 중복 검사 로직
    @GetMapping("/duplicate/login/id")
    public ResponseEntity<?> getDuplicate(@RequestParam String username){
        authService.validateDuplicateUsername(username);

        return ResponseEntity.ok("사용 가능한 아이디입니다.");
    }
}
