package com.example.shimpyo.domain.user.dto;

import com.example.shimpyo.domain.user.entity.Review;
import lombok.Builder;
import lombok.Getter;

import java.time.format.DateTimeFormatter;
import java.util.List;

@Getter
@Builder
public class ReviewDetailDto {

    private Long reviewId;
    private String createdAt;
    private String contents;
    private List<String> images;

    public static ReviewDetailDto toDto(Review review) {
        return ReviewDetailDto.builder()
                .reviewId(review.getId())
                .createdAt(review.getCreatedAt().format(DateTimeFormatter.ofPattern("yyyy.MM.dd")))
                .contents(review.getContent())
                .images(review.getImage() == null? null : review.getImage())
                .build();
    }
}
