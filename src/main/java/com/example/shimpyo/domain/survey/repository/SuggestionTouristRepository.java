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

    List<SuggestionTourist> findAllBySuggestionOrderByTimeAsc(@Param("suggestion")Suggestion suggestion);

    @Query(value = """
    SELECT image FROM (
        SELECT st.date AS date, st.time AS time, t.image AS image
        FROM suggestion_tourist st
        JOIN tourist t ON st.tourist_id = t.id
        WHERE st.suggestion_id = :suggestionId
          AND t.image IS NOT NULL

        UNION ALL

        SELECT sct.date AS date, sct.time AS time, ct.image AS image
        FROM suggestion_custom_tourist sct
        JOIN custom_tourist ct ON sct.custom_tourist_id = ct.id
        WHERE sct.suggestion_id = :suggestionId
          AND ct.image IS NOT NULL
    ) AS combined
    ORDER BY
      CAST(REGEXP_REPLACE(combined.date, '[^0-9]', '') AS UNSIGNED) ASC,
      combined.time ASC
    LIMIT 1
    """, nativeQuery = true)
    String findThumbnailBySuggestionId(@Param("suggestionId") Long suggestionId);
}
