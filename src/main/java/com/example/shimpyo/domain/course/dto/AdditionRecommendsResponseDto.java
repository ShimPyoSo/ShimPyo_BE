package com.example.shimpyo.domain.course.dto;

import com.example.shimpyo.domain.tourist.dto.OperationTime;
import com.example.shimpyo.domain.tourist.entity.Tourist;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class AdditionRecommendsResponseDto {
        private Long touristId;
        private String title;
        private String region;
        private String address;
        private OperationTime operationTime;
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
                    .tel(tourist.getTelNum())
                    .latitude(tourist.getLatitude())
                    .longitude(tourist.getLongitude())
                    .operationTime(OperationTime.toDto(tourist))
                    .images(tourist.getImage() == null? null : tourist.getImage())
                    .isLiked(false)
                    .build();
        }
}
