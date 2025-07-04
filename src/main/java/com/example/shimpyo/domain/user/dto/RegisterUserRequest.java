package com.example.shimpyo.domain.user.dto;

import com.example.shimpyo.domain.user.entity.SocialType;
import com.example.shimpyo.domain.user.entity.User;
import com.example.shimpyo.domain.user.entity.UserAuth;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class RegisterUserRequest {
    private String email;
    private String nickname;
    private String userLoginId;
    private String password;


    // [#MOO1] 사용자 등록
    public UserAuth toUserAuthEntity(String password, User user) {
        return UserAuth.builder()
                .userLoginId(this.userLoginId)
                .user(user)
                .password(password)
                .socialType(SocialType.LOCAL)
                .build();
    }
    // [#MOO1] 사용자 등록
    public User toUserEntity(){
        return User.builder()
                .email(this.email)
                .nickname(this.nickname)
                .build();
    }
}
