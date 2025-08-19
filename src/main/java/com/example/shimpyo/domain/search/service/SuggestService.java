package com.example.shimpyo.domain.search.service;

import com.example.shimpyo.domain.search.dto.SuggestItem;
import com.example.shimpyo.domain.search.repository.AcTermRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.text.Normalizer;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Service
@RequiredArgsConstructor
public class SuggestService {

    private final AcTermRepository acTermRepository;

    @Transactional(readOnly = true)
    public Set<SuggestItem> suggest(String q, int limit) {
        String nq = normalizeKo(q);
        if (nq.length() < 2) return Set.of();

        boolean isChoseong = nq.matches("^[\\u3131-\\u314E\\u1100-\\u1112]+$");

        var rows = isChoseong
                ? acTermRepository.findByChoseongPrefix(nq, limit, "tourist")
                : acTermRepository.findByNormPrefix(nq, limit, "tourist");

        Set<SuggestItem> list = new HashSet<>(rows.size());
        for (var r : rows) {
            list.add(new SuggestItem(r.term(), r.type(), r.refId(), r.weight()));
        }
        return list;
    }

    private String normalizeKo(String s) {
        if (s == null) return "";
        String t = Normalizer.normalize(s, Normalizer.Form.NFKC);
        t = t.toLowerCase();
        return t.replaceAll("[^\\p{IsHangul}\\p{IsAlphabetic}\\p{IsDigit}]", "");
    }
}
