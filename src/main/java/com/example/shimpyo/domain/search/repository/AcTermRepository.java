package com.example.shimpyo.domain.search.repository;

import com.example.shimpyo.domain.search.entity.AcTerm;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface AcTermRepository extends JpaRepository<AcTerm, Integer> {
    @Query("""
        SELECT a.term
        FROM AcTerm a
        WHERE a.termNorm LIKE CONCAT('%', :word, '%')
           OR a.termChoseong LIKE CONCAT('%', :word, '%')
    """)
    List<String> findSuggestions(@Param("word") String word, Pageable pageable);
}
