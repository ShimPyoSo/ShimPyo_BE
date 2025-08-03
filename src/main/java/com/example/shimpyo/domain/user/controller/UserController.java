package com.example.shimpyo.domain.user.controller;

import com.example.shimpyo.domain.likes.service.LikesService;
import com.example.shimpyo.domain.user.dto.TouristLikesResponseDto;
import com.example.shimpyo.domain.user.service.UserService;
import com.example.shimpyo.global.BaseException;
import com.example.shimpyo.global.exceptionType.MemberExceptionType;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@Slf4j
@RequiredArgsConstructor
@RequestMapping("/api/user/mypage")
@RestController
@Tag(name = "User", description = "유저 관련 API")
public class UserController {

    private final UserService userService;
    private final LikesService likesService;

    @PatchMapping("/nickname")
    public ResponseEntity<Void> changeNickname(@RequestBody Map<String, String> requestDto) {
        String newNickname = requestDto.get("nickname");
        if (!newNickname.matches("^[a-zA-Z0-9가-힣_]{2,20}$")) {
            throw new BaseException(MemberExceptionType.NICKNAME_NOT_VALID);
        }
        userService.changeNickname(newNickname);
        return ResponseEntity.ok().build();
    }

    @GetMapping("/duplicate/nickname")
    public ResponseEntity<Void> checkNickname(@RequestParam("nickname") String nickname) {
        if (!nickname.matches("^[a-zA-Z0-9가-힣_]{2,20}$")) {
            throw new BaseException(MemberExceptionType.NICKNAME_NOT_VALID);
        }
        userService.checkNickname(nickname);
        return ResponseEntity.ok().build();
    }

    @GetMapping("/likes")
    public ResponseEntity<List<TouristLikesResponseDto>> getTouristLikes(@RequestParam("category") String category,
                                                                         @RequestParam("likesId") Long id) {
        return ResponseEntity.ok(likesService.getTouristLikes(category, id));
    }
}
