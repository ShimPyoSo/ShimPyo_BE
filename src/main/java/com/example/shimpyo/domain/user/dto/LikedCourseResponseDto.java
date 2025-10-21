package com.example.shimpyo.domain.user.dto;

import com.example.shimpyo.domain.survey.entity.Suggestion;
import com.example.shimpyo.domain.survey.entity.SuggestionTourist;
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

    public static LikedCourseResponseDto toDto(Suggestion suggestion, String thumbnail) {
        return LikedCourseResponseDto.builder()
                .courseId(suggestion.getId())
                .title(suggestion.getTitle())
                .token(suggestion.getToken())
                .typename(suggestion.getWellnessType().getLabel())
                .thumbnail(thumbnail).build();
    }
}
