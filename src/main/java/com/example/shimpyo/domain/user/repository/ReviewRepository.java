package com.example.shimpyo.domain.user.repository;

import com.example.shimpyo.domain.tourist.entity.Tourist;
import com.example.shimpyo.domain.user.entity.Review;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ReviewRepository extends JpaRepository<Review, Long> {

    List<Review> findByTouristAndIdLessThanOrderByIdDesc(Tourist tourist, Long lastId, Pageable pageable);

    List<Review> findByTouristOrderByIdDesc(Tourist tourist, Pageable pageable);
}
