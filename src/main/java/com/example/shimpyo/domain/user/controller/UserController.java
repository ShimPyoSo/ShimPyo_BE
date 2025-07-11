package com.example.shimpyo.domain.user.controller;

import com.example.shimpyo.domain.auth.dto.MailVerifyDto;
import com.example.shimpyo.domain.auth.service.MailService;
import com.example.shimpyo.domain.user.dto.MailCodeSendDto;
import com.example.shimpyo.domain.user.service.AuthService;
import com.example.shimpyo.domain.user.dto.RegisterUserRequest;
import com.example.shimpyo.domain.user.service.UserService;
import com.example.shimpyo.global.BaseException;
import com.example.shimpyo.global.exceptionType.MemberExceptionType;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@Slf4j
@RequiredArgsConstructor
@RequestMapping("/api/user/")
@RestController
@Tag(name = "User", description = "유저 관련 API")
public class UserController {

    private final AuthService authService;
    private final MailService mailService;
    private final UserService userService;

    // [#MOO1] 사용자 회원가입 시작
    @PostMapping("/signup")
    public ResponseEntity<?> registerUser(@RequestBody RegisterUserRequest dto){
        authService.registerUser(dto);
        return ResponseEntity.ok("회원가입 완료");
    }
    // [#MOO1] 사용자 회원가입 끝

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


    @PatchMapping("/mypage")
    public ResponseEntity<Void> changeNickname(@AuthenticationPrincipal UserDetails user,
                                               @RequestBody Map<String, String> requestDto) {
        String newNickname = requestDto.get("nickname");
        if (!newNickname.matches("^[a-zA-Z0-9가-힣_]{2,20}$")) {
            throw new BaseException(MemberExceptionType.NICKNAME_NOT_VALID);
        }
        userService.changeNickname(user.getUsername(), newNickname);
        return ResponseEntity.ok().build();
    }

    @GetMapping("/nickname")
    public ResponseEntity<Boolean> checkNickname(@RequestParam("nickname") String nickname) {
        if (!nickname.matches("^[a-zA-Z0-9가-힣_]{2,20}$")) {
            throw new BaseException(MemberExceptionType.NICKNAME_NOT_VALID);
        }
        return ResponseEntity.ok(userService.checkNickname(nickname));
    }
}
