package com.example.shimpyo.domain.survey.repository;

import com.example.shimpyo.domain.survey.entity.Suggestion;
import com.example.shimpyo.domain.user.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface SuggestionRepository extends JpaRepository<Suggestion, Long> {
    boolean existsByUserAndId(@Param("user") User user, @Param("id") Long courseId);
}
