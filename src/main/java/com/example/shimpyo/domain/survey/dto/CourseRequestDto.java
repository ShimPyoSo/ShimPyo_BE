package com.example.shimpyo.domain.survey.dto;

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
    @Pattern(regexp = "^(강원도|수도권|경상도|전라도|제주도|충청도)?$",
            message = "유효한 지역명을 입력해주세요.")
    private String region;
    private Integer meal;
}
