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
    private String region;
    private List<Category> category;
    private String description;
    public boolean isLiked;

    public static LikesResponseDto toDto(Tourist tourist) {
        return LikesResponseDto.builder()
                .id(tourist.getId())
                .images(tourist.getImage())
                .region(tourist.getAddress())
                .category(tourist.getTouristCategories().stream()
                        .map(TouristCategory::getCategory)
                        .collect(Collectors.toList()))
                .description(tourist.getDescription())
                .build();
    }
}
