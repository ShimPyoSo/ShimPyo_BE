package com.example.shimpyo.domain.image.controller;

import com.example.shimpyo.domain.image.dto.ImageRequestDto;
import com.example.shimpyo.domain.image.service.S3Service;
import com.example.shimpyo.global.BaseException;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

import static com.example.shimpyo.global.exceptionType.AuthException.FILE_SIZE_OVER;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/image")
@Tag(name = "Image", description = "이미지 관련 API 목록")
public class ImageController {

    private final S3Service s3Service;

    @Operation(summary = "이미지 업로드")
    @PostMapping()
    public ResponseEntity<Map<String, String>> upload(@Valid @RequestBody ImageRequestDto requestDto) {
        if(requestDto.getFileSize()/ (1024 * 1024) > 20)
            throw new BaseException(FILE_SIZE_OVER);
        return ResponseEntity.ok(Map.of("uploadUrl", s3Service.generatePresignedUrl(requestDto)));
    }
}
