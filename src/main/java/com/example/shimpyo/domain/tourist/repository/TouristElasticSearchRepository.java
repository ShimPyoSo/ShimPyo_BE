package com.example.shimpyo.domain.tourist.repository;


import com.example.shimpyo.domain.tourist.entity.TouristDocument;
import org.springframework.data.elasticsearch.repository.ElasticsearchRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface

TouristElasticSearchRepository extends ElasticsearchRepository<TouristDocument, Long> {

}