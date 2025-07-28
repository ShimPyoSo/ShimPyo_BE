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
public class RecommendsResponseDto {
    private Long id;
    private String images;
    private String region;
    private List<Category> category;
    private String title;
    public Boolean isLiked;

    public static RecommendsResponseDto toDto(Tourist tourist) {
        return RecommendsResponseDto.builder()
                .id(tourist.getId())
                .images(null) // 임시 값
                //.images(tourist.getImage())  // 원래 값
                .region(tourist.getAddress())
                .category(tourist.getTouristCategories().stream()
                        .map(TouristCategory::getCategory)
                        .collect(Collectors.toList()))
                .title(tourist.getName())
                .isLiked(false)
                .build();
    }
}
