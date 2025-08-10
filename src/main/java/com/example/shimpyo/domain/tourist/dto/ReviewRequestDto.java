package com.example.shimpyo.domain.tourist.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Builder;
import lombok.Getter;

import java.util.List;

@Getter
@Builder
public class ReviewRequestDto {
    @NotNull(message = "id 오류입니다.")
    private Long id;
    @NotBlank(message = "5글자 이상 입력하세요.")
    private String contents;
    private List<String> images;
}
