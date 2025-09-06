package com.example.shimpyo.domain.tourist.repository;

import com.example.shimpyo.domain.tourist.entity.CustomTourist;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface CustomTouristRepository extends JpaRepository<CustomTourist, Long> {
}
