package com.example.shimpyo.domain.tourist.entity;

import jakarta.persistence.Id;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.data.elasticsearch.annotations.*;

import java.util.List;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Document(indexName = "tourist", createIndex = false)
public class TouristDocument {

    @Id
    private String id;

    // ngram analyzer 적용된 필드
    @Field(type = FieldType.Text, analyzer = "ngram_analyzer", searchAnalyzer = "autocomplete_search_analyzer")
    private String name;

    // 정확 매칭용 필드
    @Field(type = FieldType.Keyword)
    private String region;
}