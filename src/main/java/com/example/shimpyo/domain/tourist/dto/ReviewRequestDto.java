package com.example.shimpyo.domain.tourist.dto;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class ReviewRequestDto {
    private Long id;
    private String contents;
    private String images;
}
