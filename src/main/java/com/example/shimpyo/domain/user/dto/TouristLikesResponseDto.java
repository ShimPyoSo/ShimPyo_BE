package com.example.shimpyo.domain.user.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class TouristLikesResponseDto {

    private Long id;
    private Long likesId;
    private String title;
    private String region;
    private String address;
    private String openTime;
    private String closeTime;
    private String images;
}
