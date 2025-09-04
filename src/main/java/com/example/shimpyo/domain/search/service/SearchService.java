package com.example.shimpyo.domain.search.service;

import com.example.shimpyo.domain.tourist.entity.TouristDocument;
import com.example.shimpyo.domain.tourist.repository.TouristElasticSearchRepository;
import com.example.shimpyo.domain.tourist.service.TouristService;
import com.example.shimpyo.domain.utils.AutocompleteKeywordGenerator;
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

        // StringQuery 방식으로 변환
        String jsonQuery = String.format("""
        {
          "multi_match": {
            "query": "%s",
            "fields": ["nameKeywords", "regionKeywords"]
          }
        }
        """, keyword);

        StringQuery stringQuery = new StringQuery(jsonQuery);
        stringQuery.setPageable(PageRequest.of(0, 8));

        SearchHits<TouristDocument> searchHits = elasticsearchOperations.search(stringQuery, TouristDocument.class);

        return searchHits.getSearchHits()
                .stream()
                .map(SearchHit::getContent)
                .distinct()
                .sorted((a, b) -> {
                    boolean aNameStarts = a.getName().startsWith(keyword);
                    boolean bNameStarts = b.getName().startsWith(keyword);

                    if (aNameStarts && !bNameStarts) return -1;
                    if (!aNameStarts && bNameStarts) return 1;

                    boolean aRegionMatch = a.getRegionKeywords().stream().anyMatch(s -> s.contains(keyword));
                    boolean bRegionMatch = b.getRegionKeywords().stream().anyMatch(s -> s.contains(keyword));

                    if (aRegionMatch && !bRegionMatch) return -1;
                    if (!aRegionMatch && bRegionMatch) return 1;

                    return a.getName().compareTo(b.getName());
                })
                .map(TouristDocument::getName) // 항상 name 반환
                .collect(Collectors.toList());

    }

    public void indexAllTourists() {
        List<TouristDocument> documents = touristService.findAll().stream()
                .map(tourist -> TouristDocument.builder()
                        .name(tourist.getName())
                        .region(tourist.getRegion())
                        .nameKeywords(AutocompleteKeywordGenerator.generate(tourist.getName()))
                        .regionKeywords(AutocompleteKeywordGenerator.generate(tourist.getRegion()))
                        .build()
                ).collect(Collectors.toList());

        touristElasticSearchRepository.saveAll(documents);
        System.out.println("Indexed " + documents.size() + " tourist documents");
    }
}