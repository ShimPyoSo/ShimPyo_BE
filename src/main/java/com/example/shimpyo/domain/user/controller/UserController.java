package com.example.shimpyo.domain.user.controller;

import com.example.shimpyo.domain.user.service.UserService;
import com.example.shimpyo.global.BaseException;
import com.example.shimpyo.global.SwaggerErrorApi;
import com.example.shimpyo.global.exceptionType.MemberExceptionType;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@Slf4j
@RequiredArgsConstructor
@RequestMapping("/api/user/mypage")
@RestController
@Tag(name = "User", description = "유저 관련 API")
public class UserController {

    private final UserService userService;

    @Operation(summary = "닉네임 변경")
    @SwaggerErrorApi(type = {MemberExceptionType.class}, codes = {"NICKNAME_NOT_VALID", "MEMBER_NOT_FOUND"})
    @PatchMapping("/nickname")
    public ResponseEntity<Void> changeNickname(@RequestBody Map<String, String> requestDto) {
        String newNickname = requestDto.get("nickname");
        if (!newNickname.matches("^[a-zA-Z0-9가-힣_]{2,20}$")) {
            throw new BaseException(MemberExceptionType.NICKNAME_NOT_VALID);
        }
        userService.changeNickname(newNickname);
        return ResponseEntity.ok().build();
    }

    @Operation(summary = "닉네임 중복 검사")
    @GetMapping("/nickname")
    @SwaggerErrorApi(type = {MemberExceptionType.class}, codes = {"NICKNAME_NOT_VALID", "NICKNAME_DUPLICATED"})
    public ResponseEntity<Void> checkNickname(@RequestParam("nickname") String nickname) {
        if (!nickname.matches("^[a-zA-Z0-9가-힣_]{2,20}$")) {
            throw new BaseException(MemberExceptionType.NICKNAME_NOT_VALID);
        }
        userService.checkNickname(nickname);
        return ResponseEntity.ok().build();
    }
}
