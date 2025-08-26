package com.example.shimpyo.config;

import com.example.shimpyo.domain.tourist.entity.TouristDocument;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.elasticsearch.client.ClientConfiguration;
import org.springframework.data.elasticsearch.client.elc.ElasticsearchConfiguration;
import org.springframework.data.elasticsearch.core.ElasticsearchOperations;
import org.springframework.data.elasticsearch.core.IndexOperations;
import org.springframework.data.elasticsearch.core.document.Document;
import org.springframework.data.elasticsearch.repository.config.EnableElasticsearchRepositories;

import java.nio.file.Files;
import java.nio.file.Paths;
import java.time.Duration;

@Configuration
@RequiredArgsConstructor
@Slf4j
@EnableElasticsearchRepositories(basePackages = "com.example.shimpyo.domain.tourist.repository")
public class ElasticsearchConfig extends ElasticsearchConfiguration {

    @Value("${spring.elasticsearch.uris}")
    private String elasticsearchUrl;

    @Override
    public ClientConfiguration clientConfiguration() {
        return ClientConfiguration.builder()
                .connectedTo(elasticsearchUrl)  // replace 없이 바로 사용
                .withConnectTimeout(Duration.ofSeconds(30))
                .withSocketTimeout(Duration.ofSeconds(60))
                .build();
    }

    // ElasticsearchConfig에 이거 추가해서 확인
    @PostConstruct
    public void init() {
        System.out.println("=== Environment ELASTIC_HOST: " + System.getenv("ELASTIC_HOST"));
        System.out.println("=== Spring property elasticsearch.uris: " + elasticsearchUrl);
    }
}