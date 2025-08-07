package com.example.shimpyo.domain.auth.dto;

import com.example.shimpyo.domain.auth.entity.UserAuth;
import com.example.shimpyo.domain.user.entity.User;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class SocialLoginResponseDto {

    private Long userId;
    private String nickname;
    private String type;

    public static SocialLoginResponseDto toDto(UserAuth user, boolean isSignUp) {
        return SocialLoginResponseDto.builder()
                .userId(user.getUser().getId())
                .nickname(user.getUser().getNickname())
                .type(isSignUp? "signup" : "login")
                .build();
    }

    public static SocialLoginResponseDto toDto(User user) {
        return SocialLoginResponseDto.builder()
                .userId(user.getId())
                .nickname(user.getNickname())
                .build();
    }
}
