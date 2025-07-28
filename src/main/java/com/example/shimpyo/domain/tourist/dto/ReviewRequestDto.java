package com.example.shimpyo.domain.tourist.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class ReviewRequestDto {
    @NotBlank(message = "id 오류입니다.")
    private Long id;
    @NotBlank(message = "5글자 이상 입력하세요.")
    private String contents;
    private String images;
}
