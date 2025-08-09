package com.example.shimpyo.domain.user.repository;

import com.example.shimpyo.domain.tourist.entity.Tourist;
import com.example.shimpyo.domain.user.dto.MyReviewListResponseDto;
import com.example.shimpyo.domain.user.entity.Review;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ReviewRepository extends JpaRepository<Review, Long> {

    List<Review> findByTouristAndIdLessThanOrderByIdDesc(Tourist tourist, Long lastId, Pageable pageable);

    List<Review> findByTouristOrderByIdDesc(Tourist tourist, Pageable pageable);

    @Query("SELECT new com.example.shimpyo.domain.user.dto.MyReviewListResponseDto(" +
            "r.tourist.id, r.tourist.region, r.tourist.name, r.tourist.image, r.tourist.address, COUNT(r)) " +
            "FROM Review r " +
            "WHERE r.user.id = :userId " +
            "GROUP BY r.tourist.id, r.tourist.region, r.tourist.name, r.tourist.image, r.tourist.address")
    List<MyReviewListResponseDto> countReviewsByTouristForUser(@Param("userId") Long userId);
}
