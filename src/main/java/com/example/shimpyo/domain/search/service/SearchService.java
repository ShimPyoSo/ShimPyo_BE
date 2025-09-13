package com.example.shimpyo.domain.search.service;

import com.example.shimpyo.domain.tourist.entity.TouristDocument;
import com.example.shimpyo.domain.tourist.repository.TouristElasticSearchRepository;
import com.example.shimpyo.domain.tourist.service.TouristService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.elasticsearch.core.SearchHit;
import org.springframework.data.elasticsearch.core.ElasticsearchOperations;
import org.springframework.data.elasticsearch.core.SearchHits;
import org.springframework.data.elasticsearch.core.query.StringQuery;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class SearchService {

    private final ElasticsearchOperations elasticsearchOperations;
    private final TouristElasticSearchRepository touristElasticSearchRepository;
    private final TouristService touristService;

    public List<String> autoComplete(String keyword) {
        // multi_match → name(Text), region(Keyword) 대상으로 검색
        String jsonQuery = String.format("""
    {
      "multi_match": {
        "query": "%s",
        "fields": ["name^2", "region"]   // name 가중치 ↑
      }
    }
    """, keyword);

        StringQuery stringQuery = new StringQuery(jsonQuery);
        stringQuery.setPageable(PageRequest.of(0, 8));

        SearchHits<TouristDocument> searchHits =
                elasticsearchOperations.search(stringQuery, TouristDocument.class);

        return searchHits.getSearchHits()
                .stream()
                .map(SearchHit::getContent)
                .distinct()
                .sorted((a, b) -> {
                    // 1️⃣ name이 keyword로 시작하면 우선
                    boolean aNameStarts = a.getName().startsWith(keyword);
                    boolean bNameStarts = b.getName().startsWith(keyword);
                    if (aNameStarts && !bNameStarts) return -1;
                    if (!aNameStarts && bNameStarts) return 1;

                    // 2️⃣ region이 정확히 keyword랑 매칭되면 우선
                    boolean aRegionMatch = a.getRegion().equals(keyword);
                    boolean bRegionMatch = b.getRegion().equals(keyword);
                    if (aRegionMatch && !bRegionMatch) return -1;
                    if (!aRegionMatch && bRegionMatch) return 1;

                    // 3️⃣ 그 외는 name 기준 정렬
                    return a.getName().compareTo(b.getName());
                })
                .map(TouristDocument::getName) // 항상 name 반환
                .collect(Collectors.toList());
    }

    public void indexAllTourists() {
        List<TouristDocument> documents = touristService.findAll().stream()
                .map(tourist -> TouristDocument.builder()
                        .id(String.valueOf(tourist.getId()))
                        .name(tourist.getName())
                        .region(tourist.getRegion())
                        .build()
                ).collect(Collectors.toList());

        touristElasticSearchRepository.saveAll(documents);
        System.out.println("Reindexed " + documents.size() + " tourist documents");
    }
}