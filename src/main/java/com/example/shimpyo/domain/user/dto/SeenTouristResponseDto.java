package com.example.shimpyo.domain.user.dto;

import com.example.shimpyo.domain.tourist.dto.OperationTime;
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
    private OperationTime operationTime;
    private String images;

    public static SeenTouristResponseDto toDto(Tourist tourist) {
        return SeenTouristResponseDto.builder()
                .id(tourist.getId())
                .title(tourist.getName())
                .region(tourist.getRegion())
                .address(tourist.getAddress())
                .operationTime(OperationTime.toDto(tourist))
                .images(tourist.getImage())
                .build();
    }
}
