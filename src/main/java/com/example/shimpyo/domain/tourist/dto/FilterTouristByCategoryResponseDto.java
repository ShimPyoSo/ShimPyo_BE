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
public class FilterTouristByCategoryResponseDto {
    private Long id;
    private String title;
    private String type;
    private String region;
    private String operationTime;
    private String image;
    private boolean isLiked;

    public static FilterTouristByCategoryResponseDto from(Tourist tourist, boolean isLiked, String region) {
        return FilterTouristByCategoryResponseDto.builder()
                .id(tourist.getId())
                .title(tourist.getName())
//                .type(tourist)
                .region(region)
                .operationTime(tourist.getOperationTime())
                .image(tourist.getImage()) // 또는 getImages().get(0) 등
                .isLiked(isLiked)
                .build();
    }
}
