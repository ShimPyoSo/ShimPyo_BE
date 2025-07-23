package com.example.shimpyo.domain.tourist.controller;


import com.example.shimpyo.domain.tourist.dto.LikesResponseDto;
import com.example.shimpyo.domain.tourist.dto.RecommendsResponseDto;
import com.example.shimpyo.domain.tourist.service.TouristService;
import io.swagger.v3.oas.annotations.Operation;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/main")
@RequiredArgsConstructor
public class MainController {

    private final TouristService touristService;

    @GetMapping("/recommends")
    @Operation(summary = "추천 장소")
    public ResponseEntity<List<RecommendsResponseDto>> getRecommendTourists(Authentication authentication) {
        return ResponseEntity.ok(
                touristService.getRecommendTourists(authentication == null? null : authentication.getName()));
    }

    @GetMapping("/likes")
    @Operation(summary = "찜한 장소")
    public ResponseEntity<List<LikesResponseDto>> getLikesTourists(Authentication authentication) {
        return ResponseEntity.ok(touristService.getLikesTourists(authentication.getName()));
    }
}
