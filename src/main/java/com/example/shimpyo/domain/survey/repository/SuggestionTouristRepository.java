package com.example.shimpyo.domain.survey.repository;

import com.example.shimpyo.domain.survey.entity.SuggestionTourist;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface SuggestionTouristRepository extends JpaRepository<SuggestionTourist, Long> {
}
