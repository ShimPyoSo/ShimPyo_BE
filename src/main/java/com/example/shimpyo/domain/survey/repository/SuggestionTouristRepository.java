package com.example.shimpyo.domain.survey.repository;

import com.example.shimpyo.domain.survey.entity.Suggestion;
import com.example.shimpyo.domain.survey.entity.SuggestionTourist;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface SuggestionTouristRepository extends JpaRepository<SuggestionTourist, Long> {

    @Query("SELECT DISTINCT t.region " +
            "FROM SuggestionTourist st " +
            "JOIN st.tourist t " +
            "WHERE st.suggestion.id = :suggestionId")
    List<String> findDistinctRegionsBySuggestionId(@Param("suggestionId") Long suggestionId);

    SuggestionTourist findTop1BySuggestionOrderByTimeAsc(@Param("suggestion") Suggestion suggestion);
}
