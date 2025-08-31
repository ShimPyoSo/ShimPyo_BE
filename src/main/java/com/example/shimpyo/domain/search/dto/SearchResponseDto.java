package com.example.shimpyo.domain.search.dto;

import com.example.shimpyo.domain.tourist.dto.FilterTouristByDataResponseDto;
import com.example.shimpyo.domain.tourist.dto.OperationTime;
import com.example.shimpyo.domain.tourist.entity.Tourist;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class SearchResponseDto {
    private Long id;
    private String title;
    private String region;
    private String address;
    private String tel;
    private OperationTime operationTime;
    private Double latitude;
    private Double longitude;
    private String images;
    private Boolean isLiked;

    public static SearchResponseDto toDto(Tourist tourist, Boolean isLiked) {
        return SearchResponseDto.builder()
                .id(tourist.getId())
                .title(tourist.getName())
                .address(tourist.getAddress())
                .region(tourist.getRegion())
                .tel(tourist.getTelNum())
                .operationTime(OperationTime.toDto(tourist))
                .latitude(tourist.getLatitude())
                .longitude(tourist.getLongitude())
                .images(tourist.getImage())
                .isLiked(isLiked)
                .build();
    }
}
