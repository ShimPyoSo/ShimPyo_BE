package com.example.shimpyo.domain.tourist.repository;

import com.example.shimpyo.domain.tourist.entity.Category;
import com.example.shimpyo.domain.tourist.entity.TouristCategory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface TouristCategoryRepository extends JpaRepository<TouristCategory,Long> {

    @Query("""
        SELECT tc.tourist
        FROM TouristCategory tc
        WHERE (:category = 'all' OR tc.category = :category)
    """)
    List<TouristCategory> findByCategory(Category category);

}
