package com.example.shimpyo.domain.survey.dto;

import com.example.shimpyo.domain.tourist.dto.OperationTime;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Builder;
import lombok.Getter;

import java.util.List;

@Getter
public class CourseUpdateRequestDto {

    @NotNull
    private Long courseId;
    @NotNull
    @NotBlank
    private String title;
    @NotNull
    @NotBlank
    private String typename;
    @NotNull
    @NotBlank
    private String token;
    @NotNull
    private List<CourseDayDto> days;

    @Getter
    @Builder
    public static class CourseDayDto {
        @NotNull
        @NotBlank
        private String date;
        @NotNull
        private List<TouristInfoDto> list;

    }

    @Getter
    @Builder
    public static class TouristInfoDto {
        private Long touristId;
        @NotNull
        @NotBlank
        private String title;
        @NotNull
        @NotBlank
        private String time;
        private String images;
        @NotNull
        @NotBlank
        private String address;
        private String tel;
        @NotNull
        private Double latitude;
        @NotNull
        private Double longitude;
        @NotNull
        @NotBlank
        private String type;
    }
}
