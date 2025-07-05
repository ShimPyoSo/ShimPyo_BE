package com.example.shimpyo.domain.user.controller;

import com.example.shimpyo.domain.user.service.AuthService;
import com.example.shimpyo.domain.user.dto.RegisterUserRequest;
import com.example.shimpyo.global.BaseException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@Slf4j
@RequiredArgsConstructor
@RequestMapping("/api/user/")
@RestController
public class UserController {

    private final AuthService authService;

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
}
