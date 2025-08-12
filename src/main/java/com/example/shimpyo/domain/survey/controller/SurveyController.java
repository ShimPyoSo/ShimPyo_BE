package com.example.shimpyo.domain.survey.controller;

import com.example.shimpyo.domain.survey.dto.CourseRequestDto;
import com.example.shimpyo.domain.survey.dto.CourseResponseDto;
import com.example.shimpyo.domain.survey.service.SurveyService;
import io.swagger.v3.oas.annotations.Operation;
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
public class SurveyController {

    private final SurveyService surveyService;

    @Operation(summary = "코스 확인하기")
    @PostMapping("/course")
    public ResponseEntity<CourseResponseDto> getCourse(@RequestBody CourseRequestDto requestDto) {
        return ResponseEntity.ok(surveyService.getCourse(requestDto));
    }
}
