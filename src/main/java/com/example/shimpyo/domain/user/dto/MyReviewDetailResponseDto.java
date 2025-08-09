package com.example.shimpyo.domain.user.dto;

import com.example.shimpyo.domain.tourist.entity.Tourist;
import lombok.Builder;
import lombok.Getter;

import java.util.List;

@Getter
@Builder
public class MyReviewDetailResponseDto {
        private Long touristId;
        private String title;
        private String region;
        private String address;
        private List<ReviewDetailDto> reviews;

        public static MyReviewDetailResponseDto toDto(Tourist tourist, List<ReviewDetailDto> reviews) {
            return MyReviewDetailResponseDto.builder()
                    .touristId(tourist.getId())
                    .title(tourist.getName())
                    .region(tourist.getRegion())
                    .address(tourist.getAddress())
                    .reviews(reviews)
                    .build();
        }

}
