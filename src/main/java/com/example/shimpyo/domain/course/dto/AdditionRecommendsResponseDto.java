package com.example.shimpyo.domain.course.dto;

import com.example.shimpyo.domain.tourist.entity.Category;
import com.example.shimpyo.domain.tourist.entity.Tourist;
import com.example.shimpyo.domain.tourist.entity.TouristCategory;
import lombok.Builder;
import lombok.Getter;

import java.util.List;
import java.util.stream.Collectors;

@Getter
@Builder
public class AdditionRecommendsResponseDto {
        private Long touristId;
        private String title;
        private String region;
        private String address;
        private List<Category> category;
        private String operationTime;
        private String tel;
        private String images;
        private Double latitude;
        private Double longitude;
        public Boolean isLiked;

        public static AdditionRecommendsResponseDto toDto(Tourist tourist) {
            return AdditionRecommendsResponseDto.builder()
                    .touristId(tourist.getId())
                    .title(tourist.getName())
                    .region(tourist.getRegion())
                    .address(tourist.getAddress())
                    .category(tourist.getTouristCategories().stream()
                            .map(TouristCategory::getCategory)
                            .collect(Collectors.toList()))
                    .tel(tourist.getTel())
                    .latitude(tourist.getLatitude())
                    .longitude(tourist.getLongitude())
                    .operationTime(tourist.getDayOffShow())
                    .images(tourist.getImage() == null? null : tourist.getImage())
                    .isLiked(false)
                    .build();
        }
}
