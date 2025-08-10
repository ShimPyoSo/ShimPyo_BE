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
    private String type;
    private String region;
    private String address;
    private String openTime;
    private String closeTime;
    private String image;
    private Boolean isLiked;

    public static FilterTouristByDataResponseDto from(Tourist tourist, Boolean isLiked) {
        return FilterTouristByDataResponseDto.builder()
                .id(tourist.getId())
                .title(tourist.getName())
//                .type(tourist)
                .address(tourist.getAddress())
                .region(tourist.getRegion())
                .openTime(tourist.getOpenTime())
                .closeTime(tourist.getCloseTime())
                .image(tourist.getImage()) // 또는 getImages().get(0) 등
                .isLiked(isLiked)
                .build();
    }
}
