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

    @Field(type = FieldType.Text, analyzer = "autocomplete_analyzer", searchAnalyzer = "autocomplete_search_analyzer")
    private String name;

    @Field(type = FieldType.Text, analyzer = "autocomplete_analyzer", searchAnalyzer = "autocomplete_search_analyzer")
    private String region;

    @Field(type = FieldType.Keyword)
    private List<String> nameKeywords;

    @Field(type = FieldType.Keyword)
    private List<String> regionKeywords;
}