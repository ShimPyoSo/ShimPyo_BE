package com.example.shimpyo.domain.search.util;

import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

@Component
public class AddressSplitter {
    public List<String> splitRegionTokens(String address) {
        if (address == null || address.isBlank()) return List.of();
        String[] parts = address.trim().split("\\s+"); // 공백 기준
        List<String> tokens = new ArrayList<>();
        if (parts.length >= 1) tokens.add(normalizeRegion(parts[0])); // 시/도
        if (parts.length >= 2) tokens.add(parts[1]);                  // 시/군/구
        return tokens.stream().filter(s -> s != null && !s.isBlank()).distinct().toList();
    }

    private String normalizeRegion(String s) {
        return switch (s) {
            case "충청북도" -> "충북";
            case "충청남도" -> "충남";
            case "전라북도" -> "전북";
            case "전라남도" -> "전남";
            case "경상북도" -> "경북";
            case "경상남도" -> "경남";
            default -> s.replace("특별시","").replace("광역시",""); // "서울특별시" → "서울"
        };
    }
}
