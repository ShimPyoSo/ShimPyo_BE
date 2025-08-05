package com.example.shimpyo.domain.auth.controller;

import com.example.shimpyo.domain.auth.dto.*;
import com.example.shimpyo.domain.auth.service.AuthService;
import com.example.shimpyo.domain.auth.service.MailService;
import com.example.shimpyo.domain.auth.service.OAuth2Service;
import com.example.shimpyo.global.SwaggerErrorApi;
import com.example.shimpyo.global.exceptionType.AuthException;
import com.example.shimpyo.global.exceptionType.MemberExceptionType;
import com.example.shimpyo.global.exceptionType.TokenException;
import com.fasterxml.jackson.core.JsonProcessingException;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.mail.MessagingException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/user/auth")
@RequiredArgsConstructor
@Tag(name = "UserAuth", description = "인증 관련 API 목록")
public class AuthController {

    private final OAuth2Service oAuth2Service;
    private final AuthService  authService;
    private final MailService mailService;

    @Operation(summary = "소셜 로그인/회원가입")
    @PostMapping("/social/login")
    public ResponseEntity<SocialLoginResponseDto> getKaKaoToken(@RequestBody Map<String, String> requestDto,
                                                          HttpServletResponse response) throws JsonProcessingException {
        return ResponseEntity.ok(oAuth2Service.kakaoLogin(requestDto.get("accessToken"),response));
    }

    // [#MOO3] 유저 로그인 시작
    @SwaggerErrorApi(type = AuthException.class, codes = {"MEMBER_NOT_FOUND", "MEMBER_INFO_NOT_MATCHED"})
    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody UserLoginDto dto, HttpServletResponse response) {
        LoginResponseDto loginResponseDto = authService.login(dto, response);

        return ResponseEntity.ok(loginResponseDto);
    }
    // [#MOO3] 유저 로그인 끝

    // [#MOO1] 사용자 회원가입 시작
    @Operation(summary = "회원가입")
    @SwaggerErrorApi(type = AuthException.class, codes = {"EMAIL_DUPLICATION"})
    @PostMapping("/signup")
    public ResponseEntity<?> registerUser(@Valid @RequestBody RegisterUserRequest dto,
                                          HttpServletResponse response){
        authService.registerUser(dto, response);
        return ResponseEntity.ok("회원가입 완료");
    }
    // [#MOO1] 사용자 회원가입 끝N

    // [#M002] 이메일 검증 시작
    //TODO 미사용
    @Operation(summary = "이메일 검증 전송")
    @SwaggerErrorApi(type = AuthException.class, codes = {"EMAIL_DUPLICATION"})
    @GetMapping("/check/email/duplicate")
    public ResponseEntity<?> getEmail(@RequestParam String email){
        return ResponseEntity.ok(authService.emailCheck(email));
    }
    // [#M002] 이메일 검증 끝

    // [#MOO4] 이메일 인증 코드 발급 시작
    @Operation(summary = "이메일 인증 코드 발급")
    @SwaggerErrorApi(type = AuthException.class, codes = {"EMAIL_DUPLICATION", "EMAIL_NOT_FOUNDED"})
    @PostMapping("/email")
    public ResponseEntity<?> sendEmail(@RequestBody MailCodeSendDto dto){
        mailService.authEmail(dto);

        return ResponseEntity.ok("이메일을 전송하였습니다.");
    }
    // [#MOO4] 이메일 인증 코드 발급 끝

    // [#MOO5] 이메일 인증 코드 검증 시작
    @Operation(summary = "이메일 인증 코드 검증")
    @SwaggerErrorApi(type = AuthException.class, codes = {"MAIL_CODE_NOT_MATCHED"})
    @PostMapping("/email/verify")
    public ResponseEntity<?> verifyEmail(@RequestBody MailVerifyDto dto){
        mailService.verifyAuthCode(dto);

        return ResponseEntity.ok("인증 완료");
    }
    // [#MOO5] 이메일 인증 코드 검증 끝

    // 아이디 중복 검사 로직
    @Operation(summary = "아이디 중복 검사", description = "사용자 로그인 아이디를 기반으로 중복 검사.")
    @SwaggerErrorApi(type = AuthException.class, codes = {"LOGIN_ID_DUPLICATION"})
    @GetMapping("/duplicate/username")
    public ResponseEntity<?> getDuplicate(@RequestParam String username){
        authService.validateDuplicateUsername(username);

        return ResponseEntity.ok("사용 가능한 아이디입니다.");
    }

    // 아이디 찾기 로직 구현
    @Operation(summary = "아이디 찾기", description = "이메일을 기반으로 사용자의 아이디를 조회합니다.")
    @SwaggerErrorApi(type = {AuthException.class, MemberExceptionType.class},
            codes = {"INVALID_EMAIL_REQUEST", "EMAIL_NOT_FOUNDED", "MEMBER_NOT_FOUND"})
    @PostMapping("/username")
    public ResponseEntity<?> getUsername(@RequestBody FindUsernameRequestDto requestDto){
        FindUsernameResponseDto responseDto = authService.findUsername(requestDto);

        return ResponseEntity.ok(responseDto);
    }

    @Operation(summary = "비밀번호 찾기")
    @SwaggerErrorApi(type = {AuthException.class, MemberExceptionType.class},
            codes = {"EMAIL_NOT_FOUNDED", "MEMBER_NOT_FOUND"})
    @PatchMapping("/password")
    public ResponseEntity<Void> sendPasswordMail(@Valid @RequestBody FindPasswordRequestDto requestDto) throws MessagingException {
        authService.sendPasswordResetMail(requestDto);
        return ResponseEntity.ok().build();
    }

    @Operation(summary = "비밀번호 변경")
    @SwaggerErrorApi(type = {AuthException.class, MemberExceptionType.class},
            codes = {"PASSWORD_NOT_MATCHED", "TWO_PASSWORD_NOT_MATCHED", "PASSWORD_DUPLICATED", "MEMBER_NOT_FOUND",
            "SOCIAL_USER_CANT_CHANGE_PASSWORD"})
    @PutMapping("/password")
    public ResponseEntity<Void> resetPassword(@Valid @RequestBody ResetPasswordRequestDto requestDto) {
        authService.resetPassword(requestDto);
        return ResponseEntity.ok().build();
    }

    @Operation(summary = "회원 탈퇴")
    @SwaggerErrorApi(type = {MemberExceptionType.class}, codes = {"MEMBER_NOT_FOUND"})
    @DeleteMapping
    public ResponseEntity<Void> deleteUser() {
        authService.deleteUser();
        return ResponseEntity.ok().build();
    }

    @Operation(summary = "로그 아웃")
    @PostMapping("/logout")
    public ResponseEntity<?> logout(@CookieValue("access_token") String accessToken,
                                    HttpServletResponse response) {
        authService.logout(accessToken, response);
        return ResponseEntity.ok().build();
    }

    @Operation(summary = "토큰 재발급")
    @PostMapping("/reissue")
    public ResponseEntity<Void> reissue(HttpServletRequest request, HttpServletResponse response) {
        authService.refreshToken(request, response);
        return ResponseEntity.ok().build();
    }

    @Operation(summary = "자동 로그인")
    @PostMapping("/relogin")
    public ResponseEntity<?> relogin(HttpServletRequest request, HttpServletResponse response){
        authService.refreshToken(request, response);
        LoginResponseDto dto = authService.reLoginResponse(request);
        return ResponseEntity.ok(dto);
    }

    @Tag(name = "ZToken", description = "토큰 관련 예외 목록")
    @Operation(summary = "토큰 관련 예외 목록")
    @SwaggerErrorApi(type = {TokenException.class},
            codes = {"INVALID_TOKEN", "INVALID_REFRESH_TOKEN", "NOT_MATCHED_REFRESH_TOKEN", "TOKEN_IS_BLACKLISTED"})
    @GetMapping("/not-use")
    public ResponseEntity<Void> getTokenIssues() {
        return ResponseEntity.ok().build();
    }
}
