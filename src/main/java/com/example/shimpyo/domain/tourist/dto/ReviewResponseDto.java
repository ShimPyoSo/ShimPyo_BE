package com.example.shimpyo.domain.tourist.dto;

import com.example.shimpyo.domain.user.entity.Review;
import com.example.shimpyo.domain.user.entity.User;
import lombok.Builder;
import lombok.Getter;

import java.time.format.DateTimeFormatter;
import java.util.List;

@Getter
@Builder
public class ReviewResponseDto {
    private Long reviewId;
    private Long userId;
    private String nickname;
    private String createdAt;
    private String contents;
    private List<String> images;

    public static ReviewResponseDto toDto(Review review, User user) {
        return ReviewResponseDto.builder()
                .reviewId(review.getId())
                .userId(user.getId())
                .nickname(user.getDeletedAt() == null? user.getNickname() : null)
                .createdAt(review.getCreatedAt().format(DateTimeFormatter.ofPattern("yyyy.MM.dd")))
                .contents(review.getContent())
                .images(review.getImage() == null? null : review.getImage())
                .build();
    }
}
