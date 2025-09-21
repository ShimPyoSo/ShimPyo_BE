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
            "WHERE t.region IN (:regions) " +
            "AND tc.category IN (:categories) " +
            "AND t.open_time IS NOT NULL " +
            "ORDER BY RAND() " +
            "LIMIT :count",
            nativeQuery = true)
    List<Tourist> findByRegionsAndCategoriesAndOpenTimeIsNotNull(
            @Param("regions") List<String> regions,
            @Param("categories") List<Category> categories,
            @Param("count") int count);

    @Query(value = "SELECT DISTINCT t FROM Tourist t " +
            "JOIN t.touristCategories tc " +
            "WHERE t.region IN (:regions) " +
            "AND tc.category IN (:categories)")
    List<Tourist> findByRegionsAndCategories(@Param("regions") List<String> regions,
                                             @Param("categories") List<Category> categories);


    // 특정 region (예: "경북") 안의 regionDetail 목록 distinct 조회
    @Query("select distinct t.regionDetail from Tourist t where t.region = :region")
    List<String> findDistinctRegionDetailsByRegion(@Param("region") String region);

    // regionDetail + category 조건으로 관광지 조회 (join TouristCategory)
    @Query("select distinct t from Tourist t " +
            "join t.touristCategories tc " +
            "where t.region = :region and t.regionDetail = :regionDetail and tc.category in :categories")
    List<Tourist> findByRegionDetailAndCategories(@Param("region") String region,
                                                  @Param("regionDetail") String regionDetail,
                                                  @Param("categories") List<Category> categories);

}
