package com.example.shimpyo.domain.survey.repository;

import com.example.shimpyo.domain.survey.entity.Suggestion;
import com.example.shimpyo.domain.survey.entity.SuggestionUser;
import com.example.shimpyo.domain.user.dto.LikedCourseResponseDto;
import com.example.shimpyo.domain.user.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import io.lettuce.core.dynamic.annotation.Param;
import java.util.List;

@Repository
public interface SuggestionUserRepository extends JpaRepository<SuggestionUser, Long> {
    List<SuggestionUser> findByUser(@Param("user") User user);
}
