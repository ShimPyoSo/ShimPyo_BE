package com.example.shimpyo.domain.user.dto;

import com.example.shimpyo.domain.tourist.entity.Tourist;
import com.example.shimpyo.domain.user.entity.Likes;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class TouristLikesResponseDto {

    private Long id;
    private Long likesId;
    private String title;
    private String region;
    private String address;
    private String operationTime;
    private String images;

    public static TouristLikesResponseDto toDto(Likes like) {
        Tourist tourist = like.getTourist();
        return TouristLikesResponseDto.builder()
                .id(tourist.getId())
                .likesId(like.getId())
                .title(tourist.getName())
                .region(tourist.getRegion())
                .address(tourist.getAddress())
                .operationTime(tourist.getDayOffShow())
                .images(tourist.getImage())
                .build();
    }
}
