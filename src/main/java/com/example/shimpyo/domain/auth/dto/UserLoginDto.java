package com.example.shimpyo.domain.auth.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UserLoginDto {
    private String username;
    private String password;
    // 자동 로그인
    private Boolean isRememberMe;
}
