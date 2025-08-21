package com.example.shimpyo.domain.search.service;

import com.example.shimpyo.domain.search.repository.AcTermRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import java.util.List;

@Service
@RequiredArgsConstructor
public class SuggestService {

    private final AcTermRepository acTermRepository;
    public List<String> autocomplete(String word, int limit) {
        if (word == null || word.isBlank()) return List.of();

        String normalized = word.replaceAll("\\s+", "").toLowerCase();
        Pageable pageable = PageRequest.of(0, limit);

        return acTermRepository.findSuggestions(normalized, pageable)
                .stream()
                .distinct()
                .toList();
    }
}
