package com.example.shimpyo.domain.search.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class SuggestItem {
    private String text;   // 노출 텍스트
    private String type;   // "tourist" | "category" | "region" ...
    private String refId;  // 원본 엔티티 id (touristId 등)
    private int weight;    // 가중치(정렬용)
}