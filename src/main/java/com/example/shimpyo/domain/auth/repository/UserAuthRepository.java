package com.example.shimpyo.domain.auth.repository;

import com.example.shimpyo.domain.user.entity.SocialType;
import io.lettuce.core.dynamic.annotation.Param;
import com.example.shimpyo.domain.auth.entity.UserAuth;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;

import java.util.Optional;

public interface UserAuthRepository extends JpaRepository<UserAuth, Long> {

    Optional<UserAuth> findByUserLoginIdAndSocialType(String userLoginId, SocialType socialType);

    Optional<UserAuth> findByUserLoginId(String userLoginId);

    Optional<UserAuth> findByUserId(Long userId);

    @Modifying
    @Query("UPDATE UserAuth ua SET ua.lastLogin = CURRENT_TIMESTAMP WHERE ua.userLoginId = :loginId")
    void updateLastLogin(@Param("loginId") String loginId);

}
