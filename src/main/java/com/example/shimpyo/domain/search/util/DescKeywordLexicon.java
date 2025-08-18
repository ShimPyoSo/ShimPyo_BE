package com.example.shimpyo.domain.search.util;

import org.springframework.stereotype.Component;

import java.util.LinkedHashSet;
import java.util.Set;

@Component
public class DescKeywordLexicon {

    private static final Set<String> KEYWORDS = Set.of(
            "명상","힐링","숲","산책","스파","온천","요가","호흡","체험","자연","휴식","프리미엄","리조트"
    );

    public Set<String> extractFrom(String description) {
        if (description == null || description.isBlank()) return Set.of();
        // 아주 단순: 포함 여부 매칭 (고급화는 형태소/토큰화로)
        Set<String> hit = new LinkedHashSet<>();
        for (String k : KEYWORDS) {
            if (description.contains(k)) hit.add(k);
        }
        return hit;
    }
}
