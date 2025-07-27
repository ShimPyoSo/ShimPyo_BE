package com.example.shimpyo.domain.tourist.dto;

import com.example.shimpyo.domain.user.entity.Review;
import com.example.shimpyo.domain.user.entity.User;
import lombok.Builder;
import lombok.Getter;

import java.time.format.DateTimeFormatter;

@Getter
@Builder
public class ReviewResponseDto {
    private String nickname;
    private String createdAt;
    private String contents;
    private String images;

    public static ReviewResponseDto toDto(Review review, User user) {
        return ReviewResponseDto.builder()
                .nickname(user.getNickname())
                .createdAt(review.getCreatedAt().format(DateTimeFormatter.ofPattern("yyyy.MM.dd")))
                .contents(review.getContent())
                .images(String.join(", ", review.getImage()))
                .build();
    }
}
