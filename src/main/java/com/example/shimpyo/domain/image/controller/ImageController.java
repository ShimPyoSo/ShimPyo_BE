package com.example.shimpyo.domain.image.controller;

import com.example.shimpyo.domain.image.dto.ImageRequestDto;
import com.example.shimpyo.domain.image.service.S3Service;
import io.swagger.v3.oas.annotations.Operation;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/image")
public class ImageController {

    private final S3Service s3Service;

    @Operation(summary = "이미지 업로드")
    @PostMapping()
    public ResponseEntity<Map<String, String>> upload(@Valid @RequestBody ImageRequestDto requestDto) {
        return ResponseEntity.ok(Map.of("uploadUrl", s3Service.generatePresignedUrl(requestDto)));
    }
}
