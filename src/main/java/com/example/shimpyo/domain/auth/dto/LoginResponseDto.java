package com.example.shimpyo.domain.auth.dto;

import com.example.shimpyo.domain.auth.entity.UserAuth;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class LoginResponseDto {

    private Long userId;
    private String nickname;

    public static LoginResponseDto toDto(UserAuth user) {
        return LoginResponseDto.builder()
                .userId(user.getUser().getId())
                .nickname(user.getUser().getNickname())
                .build();
    }
}
