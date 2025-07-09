package com.example.shimpyo.domain.user.controller;

import com.example.shimpyo.domain.user.service.UserService;
import com.example.shimpyo.global.BaseException;
import com.example.shimpyo.global.exceptionType.MemberExceptionType;
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
public class UserController {

    private final UserService userService;

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
