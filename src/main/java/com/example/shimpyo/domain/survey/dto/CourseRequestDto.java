package com.example.shimpyo.domain.survey.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Pattern;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
@AllArgsConstructor
public class CourseRequestDto {
    private String typename;
    @Pattern(regexp = "([1-4])박([2-5])일", message = "1박2일부터 4박5일까지 형식에 맞게 입력해주세요.")
    private String duration;
    @ValidRegion
    private String region;
    @Min(1)
    @Max(3)
    private Integer meal;
}
