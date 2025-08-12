package com.example.shimpyo.domain.survey.repository;

import com.example.shimpyo.domain.survey.entity.SurveyResult;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface SurveyRepository extends JpaRepository<SurveyResult, Long> {
}
