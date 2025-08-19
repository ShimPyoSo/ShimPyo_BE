package com.example.shimpyo.domain.search.repository;

import com.example.shimpyo.domain.search.entity.AcTerm;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

@Repository
public interface AcTermJpaRepository extends JpaRepository<AcTerm, Integer> {
    @Modifying
    @Query("delete from AcTerm t where t.type=:type and t.refId=:refId")
    void deleteByTypeAndRefId(String type, String refId);
    boolean existsByTypeAndRefIdAndTerm(String type, String refId, String term);
}
