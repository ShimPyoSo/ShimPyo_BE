package com.example.shimpyo.domain.auth.repository;

import com.example.shimpyo.domain.user.entity.SocialType;
import com.example.shimpyo.domain.auth.entity.UserAuth;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface UserAuthRepository extends JpaRepository<UserAuth, Long> {

    Optional<UserAuth> findByUserLoginIdAndSocialType(String userLoginId, SocialType socialType);

    Optional<UserAuth> findByUserLoginId(String userLoginId);
}
