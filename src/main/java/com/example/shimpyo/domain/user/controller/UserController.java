package com.example.shimpyo.domain.user.controller;

import com.example.shimpyo.domain.course.service.LikesService;
import com.example.shimpyo.domain.tourist.service.TouristService;
import com.example.shimpyo.domain.user.dto.MyReviewDetailResponseDto;
import com.example.shimpyo.domain.user.dto.MyReviewListResponseDto;
import com.example.shimpyo.domain.user.dto.SeenTouristResponseDto;
import com.example.shimpyo.domain.user.dto.TouristLikesResponseDto;
import com.example.shimpyo.domain.user.service.UserService;
import com.example.shimpyo.global.BaseException;
import com.example.shimpyo.global.SwaggerErrorApi;
import com.example.shimpyo.global.exceptionType.MemberExceptionType;
import com.example.shimpyo.global.exceptionType.TouristException;
import io.swagger.v3.oas.annotations.Operation;
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
    private final TouristService touristService;

    @Operation(summary = "닉네임 변경")
    @SwaggerErrorApi(type = {MemberExceptionType.class}, codes = {"NICKNAME_NOT_VALID", "MEMBER_NOT_FOUND"})
    @PatchMapping("/nickname")
    public ResponseEntity<Void> changeNickname(@RequestBody Map<String, String> requestDto) {
        String newNickname = requestDto.get("nickname");
        if (!newNickname.matches("^[a-zA-Z0-9가-힣_]{2,8}$")) {
            throw new BaseException(MemberExceptionType.NICKNAME_NOT_VALID);
        }
        userService.changeNickname(newNickname);
        return ResponseEntity.ok().build();
    }

    @Operation(summary = "닉네임 중복 검사")
    @SwaggerErrorApi(type = {MemberExceptionType.class}, codes = {"NICKNAME_NOT_VALID", "NICKNAME_DUPLICATED"})
    @GetMapping("/duplicate/nickname")
    public ResponseEntity<Void> checkNickname(@RequestParam("nickname") String nickname) {
        if (!nickname.matches("^[a-zA-Z0-9가-힣_]{2,8}$")) {
            throw new BaseException(MemberExceptionType.NICKNAME_NOT_VALID);
        }
        userService.checkNickname(nickname);
        return ResponseEntity.ok().build();
    }

    @Operation(summary = "찜한 관광지 목록")
    @SwaggerErrorApi(type = {MemberExceptionType.class}, codes = {"MEMBER_NOT_FOUND"})
    @GetMapping("/likes")
    public ResponseEntity<List<TouristLikesResponseDto>> getTouristLikes(@RequestParam("category") String category,
                                                                         @RequestParam("likesId") Long id) {
        return ResponseEntity.ok(likesService.getTouristLikes(category, id));
    }

    @Operation(summary = "최근 본 관광지")
    @SwaggerErrorApi(type = {TouristException.class}, codes = {"TOURIST_NOT_FOUND"})
    @PostMapping("/tourist")
    public ResponseEntity<List<SeenTouristResponseDto>> getLastSeenTourists(@RequestBody List<Long> touristIds){//SeenTouristRequestDto requestDto) {
        return ResponseEntity.ok(userService.getLastSeenTourists(touristIds));
    }

    @Operation(summary = "내가 쓴 후기 목록")
    @SwaggerErrorApi(type = {MemberExceptionType.class}, codes = {"MEMBER_NOT_FOUND"})
    @GetMapping("/review")
    public ResponseEntity<List<MyReviewListResponseDto>> getMyReviewList() {
        return ResponseEntity.ok(touristService.getMyReviewLists());
    }

    @Operation(summary = "내가 쓴 후기 상세")
    @SwaggerErrorApi(type = {MemberExceptionType.class, TouristException.class},
            codes = {"MEMBER_NOT_FOUND", "TOURIST_NOT_FOUND", "REVIEW_NOT_FOUND"})
    @GetMapping("/review-detail")
    public ResponseEntity<MyReviewDetailResponseDto> getMyReviewDetail(@RequestParam("touristId") Long touristId) {
        return ResponseEntity.ok(userService.getMyReviewTourists(touristId));
    }
}
