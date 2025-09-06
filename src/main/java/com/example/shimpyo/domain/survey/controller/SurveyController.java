package com.example.shimpyo.domain.survey.controller;

import com.example.shimpyo.domain.survey.dto.CourseRequestDto;
import com.example.shimpyo.domain.survey.dto.CourseResponseDto;
import com.example.shimpyo.domain.survey.service.SuggestionService;
import com.example.shimpyo.domain.survey.service.SurveyService;
import com.example.shimpyo.global.SwaggerErrorApi;
import com.example.shimpyo.global.exceptionType.CommonException;
import com.example.shimpyo.global.exceptionType.CourseException;
import com.example.shimpyo.global.exceptionType.MemberException;
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

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/survey")
@Tag(name = "Survey", description = "코스 추천 관련 API 목록")
public class SurveyController {

    private final SurveyService surveyService;
    private final SuggestionService suggestionService;

    @Operation(summary = "코스 확인하기")
    @SwaggerErrorApi(type = {MemberException.class}, codes = {"MEMBER_NOT_FOUND"})
    @PostMapping("/course")
    public ResponseEntity<CourseResponseDto> getCourse(@Valid @RequestBody CourseRequestDto requestDto) {
        return ResponseEntity.ok(surveyService.getCourse(requestDto));
    }

    @Operation(summary = "코스 찜하기")
    @SwaggerErrorApi(type = {MemberException.class, CourseException.class, CommonException.class},
            codes = {"MEMBER_NOT_FOUND", "ALREADY_LIKED", "COURSE_NOT_FOUND", "SERVER_ERROR"})
    @PostMapping
    public ResponseEntity<Map<String, Long>> likeCourse(@RequestBody Map<String, String> requestDto) {
        return ResponseEntity.ok(Map.of("courseId", suggestionService.likeCourse(requestDto.get("token"))));
    }
}
