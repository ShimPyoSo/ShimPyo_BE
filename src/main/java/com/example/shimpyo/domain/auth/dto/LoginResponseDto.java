package com.example.shimpyo.domain.auth.dto;

import com.example.shimpyo.domain.auth.entity.UserAuth;
import com.example.shimpyo.domain.user.entity.User;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class LoginResponseDto {

    private Long userId;
    private String nickname;

    public static LoginResponseDto toDto(User user) {
        return LoginResponseDto.builder()
                .userId(user.getId())
                .nickname(user.getNickname())
                .build();
    }
}
