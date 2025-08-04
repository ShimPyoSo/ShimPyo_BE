package com.example.shimpyo.domain.course.controller;

import com.example.shimpyo.domain.course.service.LikesService;
import com.example.shimpyo.global.SwaggerErrorApi;
import com.example.shimpyo.global.exceptionType.MemberExceptionType;
import com.example.shimpyo.global.exceptionType.TouristException;
import io.swagger.v3.oas.annotations.Operation;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/course")
public class CourseController {
    private final LikesService likesService;

    @Operation(summary = "관광지 찜 추가/삭제")
    @SwaggerErrorApi(type = {TouristException.class, MemberExceptionType.class},
            codes = {"TOURIST_NOT_FOUND", "MEMBER_NOT_FOUND"})
    @PatchMapping("/tourist")
    public ResponseEntity<Void> toggleLikeTourist(@RequestBody Map<String, Long> requestDto) {
        likesService.toggleLikeTourist(requestDto.get("id"));
        return ResponseEntity.ok().build();
    }
}
