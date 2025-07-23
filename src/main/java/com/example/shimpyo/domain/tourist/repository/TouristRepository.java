package com.example.shimpyo.domain.tourist.repository;

import com.example.shimpyo.domain.tourist.entity.Tourist;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface TouristRepository extends JpaRepository<Tourist, Long> {

    @Query(value = "SELECT * FROM tourist ORDER BY RAND() LIMIT 8", nativeQuery = true)
    List<Tourist> findRandom8Recommends();

}
