package com.example.shimpyo.domain.auth.controller;

import com.example.shimpyo.domain.auth.dto.*;
import com.example.shimpyo.domain.auth.service.MailService;
import com.example.shimpyo.domain.auth.service.OAuth2Service;
import com.example.shimpyo.domain.auth.service.AuthService;
import com.fasterxml.jackson.core.JsonProcessingException;
import io.swagger.v3.oas.annotations.Operation;
import jakarta.mail.MessagingException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
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
    public ResponseEntity<?> registerUser(@Valid @RequestBody RegisterUserRequest dto){
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
    @Operation(summary = "아이디 중복 검사", description = "사용자 로그인 아이디를 기반으로 중복 검사.")
    @GetMapping("/duplicate/username")
    public ResponseEntity<?> getDuplicate(@RequestParam String username){
        authService.validateDuplicateUsername(username);

        return ResponseEntity.ok("사용 가능한 아이디입니다.");
    }

    // 아이디 찾기 로직 구현
    @Operation(summary = "아이디 찾기", description = "이메일을 기반으로 사용자의 아이디를 조회합니다.")
    @PostMapping("/username")
    public ResponseEntity<?> getUsername(@RequestBody FindUsernameRequestDto requestDto){
        FindUsernameResponseDto responseDto = authService.findUsername(requestDto);

        return ResponseEntity.ok(responseDto);
    }

    @Operation(summary = "비밀번호 찾기")
    @PatchMapping("/password")
    public ResponseEntity<Void> sendPasswordMail(@Valid @RequestBody FindPasswordRequestDto requestDto) throws MessagingException {
        authService.sendPasswordResetMail(requestDto);
        return ResponseEntity.ok().build();
    }

    @Operation(summary = "비밀번호 변경")
    @PutMapping("/password")
    public ResponseEntity<Void> resetPassword(@Valid @RequestBody ResetPasswordRequestDto requestDto) {
        SecurityContextHolder.getContext().getAuthentication();
        authService.resetPassword(requestDto);
        return ResponseEntity.ok().build();
    }

    @Operation(summary = "회원 탈퇴")
    @DeleteMapping
    public ResponseEntity<Void> deleteUser(@RequestParam String username) {
        authService.deleteUser(username);
        return ResponseEntity.ok().build();
    }
}
