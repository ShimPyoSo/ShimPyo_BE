package com.example.shimpyo.domain.course.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class ChangeTitleRequestDto {

    @NotNull(message = "id 오류입니다.")
    private Long courseId;

    @Size(min = 2, max = 15, message = "2자 이상 15자 이하로 입력해야 합니다.")
    private String title;
}
