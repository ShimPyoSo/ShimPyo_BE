package com.example.shimpyo.domain.user.dto;

import com.example.shimpyo.domain.survey.entity.Suggestion;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class LikedCourseResponseDto {

    private Long courseId;
    private String title;
    private String typename;
    private String token;
    private String thumbnail;

    public static LikedCourseResponseDto toDto(Suggestion s, String thumbnail) {
        return LikedCourseResponseDto.builder()
                .courseId(s.getId())
                .title(s.getTitle())
                .typename(s.getWellnessType().getLabel())
                .token(s.getToken())
                .thumbnail(thumbnail)
                .build();
    }
}
