package com.example.shimpyo.domain.survey.dto;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class SuggestionRedisDto {
    private Long userId;
    private CourseResponseDto course;
}