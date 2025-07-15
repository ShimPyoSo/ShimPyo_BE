package com.example.shimpyo.domain.auth.dto;

import jakarta.validation.constraints.Pattern;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class ResetPasswordRequestDto {

    private String nowPassword;

    @Pattern(regexp = "^(?=.*[A-Za-z])(?=.*\\d)(?=.*[~!@#$%^&*])[A-Za-z\\d~!@#$%^&*]{8,}$",
            message = "비밀번호 형식이 일치하지 않습니다.")
    private String newPassword;

    private String checkNewPassword;
}
