package com.example.shimpyo.domain.likes.controller;

import com.example.shimpyo.domain.likes.service.LikesService;
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
@RequestMapping("/api/likes")
public class LikesController {
    private final LikesService likesService;

    @PatchMapping
    @Operation(summary = "관광지 찜 추가/삭제")
    public ResponseEntity<Void> toggleLikeTourist(@RequestBody Map<String, Long> requestDto) {
        likesService.toggleLikeTourist(requestDto.get("id"));
        return ResponseEntity.ok().build();
    }
}
