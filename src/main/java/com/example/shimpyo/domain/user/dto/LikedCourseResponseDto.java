package com.example.shimpyo.domain.user.dto;

import com.example.shimpyo.domain.survey.entity.WellnessType;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class LikedCourseResponseDto {

    private Long courseId;
    private String title;
    private String typename;
    private String token;
    private String thumbnail;

    public LikedCourseResponseDto(Long courseId, String title, WellnessType wellnessType, String token, String thumbnail) {
        this.courseId = courseId;
        this.title = title;
        this.typename = wellnessType.getLabel();
        this.token = token;
        this.thumbnail = thumbnail;
    }
}
