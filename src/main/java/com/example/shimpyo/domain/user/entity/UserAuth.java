package com.example.shimpyo.domain.user.entity;

import com.example.shimpyo.domain.common.BaseEntity;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@NoArgsConstructor
@AllArgsConstructor
@Entity
@Builder
@Getter
public class UserAuth extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotNull
    private String userLoginId;

    private String password;
    private LocalDateTime lastLogin;

    @NotNull
    @Enumerated(value = EnumType.STRING)
    private SocialType socialType = SocialType.LOCAL;

    @Column
    private String oauthId;

    @OneToOne
    @MapsId
    @JoinColumn(name = "user_id")
    private User user;

    @PreUpdate
    public void onLogin(){
        this.lastLogin = LocalDateTime.now();
    }
}
