package com.example.shimpyo.domain.course.repository;

import com.example.shimpyo.domain.tourist.entity.Category;
import com.example.shimpyo.domain.tourist.entity.Tourist;
import com.example.shimpyo.domain.user.entity.Likes;
import com.example.shimpyo.domain.user.entity.User;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.Set;

@Repository
public interface LikesRepository extends JpaRepository<Likes, Long> {

    Optional<Likes> findByUserAndTourist(User user, Tourist tourist);

    @Query("""
                SELECT l
                FROM Likes l
                JOIN l.tourist t
                JOIN t.touristCategories tc
                WHERE l.user = :user
                  AND tc.category = :category
                  AND (:cursor IS NULL OR l.id < :cursor)
                ORDER BY l.id DESC
            """)
    List<Likes> findLikesDtoByUserAndCategory(
            @Param("user") User user,
            @Param("cursor") Long cursor,
            @Param("category") Category category,
            Pageable pageable
    );

    @Query("""
                SELECT l
                FROM Likes l
                JOIN l.tourist t
                WHERE l.user = :user
                AND (:cursor IS NULL OR l.id < :cursor)
                ORDER BY l.id DESC
            """)
    List<Likes> findLikesDtoByUser(
            @Param("user") User user,
            @Param("cursor") Long cursor,
            Pageable pageable
    );


    @Query("select l.tourist.id from Likes l where l.user.id = :userId and l.tourist.id in :touristIds")
    Set<Long> findLikedTouristIds(@Param("userId") Long userId, @Param("touristIds") Collection<Long> touristIds);

}
