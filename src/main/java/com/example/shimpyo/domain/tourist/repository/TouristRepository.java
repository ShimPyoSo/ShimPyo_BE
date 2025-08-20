package com.example.shimpyo.domain.tourist.repository;

import com.example.shimpyo.domain.tourist.entity.Category;
import com.example.shimpyo.domain.tourist.entity.Tourist;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface TouristRepository extends JpaRepository<Tourist, Long> , JpaSpecificationExecutor<Tourist> {

    @Query(value = "SELECT * FROM tourist ORDER BY RAND() LIMIT 8", nativeQuery = true)
    List<Tourist> findRandom8Recommends();

    @Query(value = "SELECT DISTINCT t.* " +
            "FROM tourist t " +
            "JOIN tourist_category tc ON t.id = tc.tourist_id " +
            "WHERE (:regions IS NULL OR t.region IN (:regions)) " +
            "AND tc.category IN (:categories) " +
            "AND t.open_time IS NOT NULL " +
            "ORDER BY RAND() " +
            "LIMIT :count",
            nativeQuery = true)
    List<Tourist> findByRegionsAndCategoriesAndOpenTimeIsNotNull(
            @Param("regions") List<String> regions,
            @Param("categories") List<Category> categories,
            @Param("count") int count);
}
