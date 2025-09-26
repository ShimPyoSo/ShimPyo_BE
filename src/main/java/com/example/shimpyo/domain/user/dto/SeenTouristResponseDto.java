package com.example.shimpyo.domain.user.dto;

import com.example.shimpyo.domain.tourist.entity.Tourist;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class SeenTouristResponseDto {

    private Long id;
    private String title;
    private String region;
    private String address;
    private String operationTime;
    private String images;
    private Boolean isLiked;

    public static SeenTouristResponseDto toDto(Tourist tourist, Boolean isLiked) {
        return SeenTouristResponseDto.builder()
                .id(tourist.getId())
                .title(tourist.getName())
                .region(tourist.getRegion())
                .address(tourist.getAddress())
                .operationTime(tourist.getDayOffShow())
                .images(tourist.getImage())
                .isLiked(isLiked)
                .build();
    }
}
