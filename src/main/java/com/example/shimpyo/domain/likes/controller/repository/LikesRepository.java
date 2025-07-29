package com.example.shimpyo.domain.likes.controller.repository;

import com.example.shimpyo.domain.tourist.entity.Tourist;
import com.example.shimpyo.domain.user.entity.Likes;
import com.example.shimpyo.domain.user.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface LikesRepository extends JpaRepository<Likes, Long> {

    Optional<Likes> findByUserAndTourist(User user, Tourist tourist);
}
