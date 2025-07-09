package com.example.shimpyo.domain.auth.dto;

import com.example.shimpyo.domain.user.entity.SocialType;
import com.example.shimpyo.domain.user.entity.User;
import com.example.shimpyo.domain.auth.entity.UserAuth;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class RegisterUserRequest {
    private String email;
    private String username;
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
