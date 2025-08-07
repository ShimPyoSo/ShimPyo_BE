package com.example.shimpyo.domain.user.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class MyReviewListResponseDto {
    private Long touristId;
    private String region;
    private String title;
    private String images;
    private String address;
    private Long counts;
}
