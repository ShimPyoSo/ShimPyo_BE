package com.example.shimpyo.domain.user.controller;

import com.example.shimpyo.domain.user.service.AuthService;
import com.example.shimpyo.domain.user.dto.RegisterUserRequest;
import com.example.shimpyo.global.BaseException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@Slf4j
@RequiredArgsConstructor
@RequestMapping("/api/user/")
@RestController
public class UserController {

    private final AuthService authService;

    // [#MOO1] 사용자 회원가입 시작
    @PostMapping("/signup")
    public ResponseEntity<?> registerMember(@RequestBody RegisterUserRequest dto){
        authService.registerMember(dto);

        return new ResponseEntity<>("signup success!!", HttpStatus.OK);
    }
    // [#MOO1] 사용자 회원가입 끝

    // [#M002] 이메일 검증 시작
    @GetMapping("/check/email")
    public ResponseEntity<?> getEmail(@RequestParam String email){
        try{
            return ResponseEntity.ok(authService.emailCheck(email));
        }catch (BaseException e){
            return new ResponseEntity<>(e.getMessage(), HttpStatus.CONFLICT);
        }
    }
    // [#M002] 이메일 검증 끝
}
