package com.example.shimpyo.domain.survey.repository;

import com.example.shimpyo.domain.survey.entity.Suggestion;
import com.example.shimpyo.domain.survey.entity.SuggestionUser;
import com.example.shimpyo.domain.user.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface SuggestionUserRepository extends JpaRepository<SuggestionUser, Long> {
    boolean existsByUserAndSuggestion(User user, Suggestion suggestion);
}
