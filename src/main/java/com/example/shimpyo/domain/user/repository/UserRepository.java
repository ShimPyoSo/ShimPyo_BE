package com.example.shimpyo.domain.user.repository;

import com.example.shimpyo.domain.auth.entity.UserAuth;
import com.example.shimpyo.domain.user.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {

    // Email 이 일치하면서 삭제되지 않은 사용자
    Optional<User> findByEmailAndDeletedAtIsNull(String email);

    Optional<User> findByUserAuth(UserAuth userAuth);

    Optional<User> findByEmail(String email);

    boolean existsByNickname(String nickname);
}
