package com.example.shimpyo.domain.auth.dto;

import com.example.shimpyo.domain.user.entity.SocialType;
import com.example.shimpyo.domain.user.entity.User;
import com.example.shimpyo.domain.auth.entity.UserAuth;
import jakarta.validation.constraints.Pattern;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class RegisterUserRequest {
    private String email;

    @Pattern(regexp = "^[a-z0-9]{6,12}$", message = "아이디가 유효하지 않습니다.[영 소문자 + 숫자 (6~12자)]")
    private String username;

    @Pattern(regexp = "^(?=.*[A-Za-z])(?=.*\\d)(?=.*[~!@#$%^&*])[A-Za-z\\d~!@#$%^&*]{8,}$",
    message = "비밀번호 형식이 일치하지 않습니다.")
    private String password;


    // [#MOO1] 사용자 등록
    public UserAuth toUserAuthEntity(String password, User user) {
        return UserAuth.builder()
                .userLoginId(this.username)
                .user(user)
                .password(password)
                .socialType(SocialType.LOCAL)
                .build();
    }
    // [#MOO1] 사용자 등록
    public User toUserEntity(String nickname){
        return User.builder()
                .email(this.email)
                .nickname(nickname)
                .build();
    }
}
