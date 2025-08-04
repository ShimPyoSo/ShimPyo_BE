package com.example.shimpyo.domain.tourist.controller;


import com.example.shimpyo.domain.tourist.dto.LikesResponseDto;
import com.example.shimpyo.domain.tourist.dto.RecommendsResponseDto;
import com.example.shimpyo.domain.tourist.service.TouristService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/main")
@RequiredArgsConstructor
@Tag(name = "Main", description = "메인 화면 API 목록")
public class MainController {

    private final TouristService touristService;

    @GetMapping("/recommends")
    @Operation(summary = "추천 장소")
    public ResponseEntity<List<RecommendsResponseDto>> getRecommendTourists() {
        return ResponseEntity.ok(
                touristService.getRecommendTourists());
    }

    @GetMapping("/likes")
    @Operation(summary = "찜한 장소")
    public ResponseEntity<List<LikesResponseDto>> getLikesTourists() {
        return ResponseEntity.ok(touristService.getLikesTourists());
    }
}
