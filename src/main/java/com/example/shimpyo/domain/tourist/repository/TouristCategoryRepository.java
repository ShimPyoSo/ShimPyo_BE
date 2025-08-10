package com.example.shimpyo.domain.tourist.repository;

import com.example.shimpyo.domain.tourist.entity.TouristCategory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface TouristCategoryRepository extends JpaRepository<TouristCategory,Long> {

}
