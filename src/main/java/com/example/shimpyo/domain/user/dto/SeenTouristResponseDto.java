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
    private String openTime;
    private String closeTime;
    private String images;

    public static SeenTouristResponseDto toDto(Tourist tourist) {
        return SeenTouristResponseDto.builder()
                .id(tourist.getId())
                .region(tourist.getRegion())
                .address(tourist.getAddress())
                .openTime(tourist.getOpenTime())
                .closeTime(tourist.getCloseTime())
                .images(tourist.getImage())
                .build();
    }
}
