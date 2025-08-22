package com.example.shimpyo.domain.tourist.dto;

import com.example.shimpyo.domain.tourist.entity.Tourist;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class FilterTouristByDataResponseDto {
    private Long id;
    private String title;
    private String region;
    private String address;
    private OperationTime operationTime;
    private String image;
    private Boolean isLiked;

    public static FilterTouristByDataResponseDto from(Tourist tourist, Boolean isLiked) {
        return FilterTouristByDataResponseDto.builder()
                .id(tourist.getId())
                .title(tourist.getName())
                .address(tourist.getAddress())
                .region(tourist.getRegion())
                .operationTime(OperationTime.toDto(tourist))
                .image(tourist.getImage())
                .isLiked(isLiked)
                .build();
    }
}
