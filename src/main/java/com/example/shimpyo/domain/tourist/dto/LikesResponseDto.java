package com.example.shimpyo.domain.tourist.dto;

import com.example.shimpyo.domain.tourist.entity.Category;
import com.example.shimpyo.domain.tourist.entity.Tourist;
import com.example.shimpyo.domain.tourist.entity.TouristCategory;
import lombok.Builder;
import lombok.Getter;

import java.util.List;
import java.util.stream.Collectors;

@Getter
@Builder
public class LikesResponseDto {

    private Long id;
    private String images;
    private String title;
    private String region;
    private List<Category> category;
    public boolean isLiked;

    public static LikesResponseDto toDto(Tourist tourist) {
        return LikesResponseDto.builder()
                .id(tourist.getId())
                .images(tourist.getImage())
                .title(tourist.getName())
                .region(tourist.getRegion())
                .category(tourist.getTouristCategories().stream()
                        .map(TouristCategory::getCategory)
                        .collect(Collectors.toList()))
                .isLiked(true)
                .build();
    }
}
