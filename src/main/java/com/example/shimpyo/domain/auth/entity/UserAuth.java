package com.example.shimpyo.domain.auth.entity;

import com.example.shimpyo.domain.common.BaseEntity;
import com.example.shimpyo.domain.user.entity.SocialType;
import com.example.shimpyo.domain.user.entity.User;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.SQLDelete;
import org.hibernate.annotations.SQLRestriction;

import java.time.LocalDateTime;

@NoArgsConstructor
@AllArgsConstructor
@Entity
@Builder
@Getter
@SQLDelete(sql = "UPDATE user_auth SET deleted_at = now(),oauth_id = null, password = null, user_login_id = UUID() WHERE user_id = ?")
@SQLRestriction("deleted_at IS NULL")
public class UserAuth extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
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

    public void resetPassword(String newPassword) {
        this.password = newPassword;
    }
}
