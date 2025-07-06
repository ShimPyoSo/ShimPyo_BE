package com.example.shimpyo.domain.user.repository;

import com.example.shimpyo.domain.user.entity.SocialType;
import com.example.shimpyo.domain.user.entity.UserAuth;
import io.lettuce.core.dynamic.annotation.Param;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;

import java.util.Optional;

public interface UserAuthRepository extends JpaRepository<UserAuth, Long> {

    Optional<UserAuth> findByUserLoginIdAndSocialType(String userLoginId, SocialType socialType);

    Optional<UserAuth> findByUserLoginId(String userLoginId);

    @Modifying
    @Query("UPDATE UserAuth ua SET ua.lastLogin = CURRENT_TIMESTAMP WHERE ua.userLoginId = :loginId")
    void updateLastLogin(@Param("loginId") String loginId);
}
