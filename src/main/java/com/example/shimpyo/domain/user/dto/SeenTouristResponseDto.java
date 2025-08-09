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

    public static SeenTouristResponseDto toDto(Tourist tourist) {
        return SeenTouristResponseDto.builder()
                .id(tourist.getId())
                .region(tourist.getAddress())
                .address(tourist.getAddress())
                .operationTime(tourist.getOperationTime())
                .images(tourist.getImage())
                .build();
    }
}
