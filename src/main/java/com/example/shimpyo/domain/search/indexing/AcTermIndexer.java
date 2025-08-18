package com.example.shimpyo.domain.search.indexing;

import com.example.shimpyo.domain.search.entity.AcTerm;
import com.example.shimpyo.domain.search.repository.AcTermJpaRepository;
import com.example.shimpyo.domain.search.util.AddressSplitter;
import com.example.shimpyo.domain.search.util.DescKeywordLexicon;
import com.example.shimpyo.domain.tourist.entity.Tourist;
import com.example.shimpyo.domain.tourist.repository.TouristRepository;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.*;

@Slf4j
@Service
@RequiredArgsConstructor
public class AcTermIndexer {

    private final AcTermJpaRepository acTermRepo;
    private final DescKeywordLexicon lexicon;
    private final AddressSplitter addressSplitter;
    private final TouristRepository touristRepository;

    @PersistenceContext
    private EntityManager em;

    /** ▶ 실행/스케줄 시 쓰는 전체 색인 (무파라미터) */
    @Transactional
    public void reindexTourists() {
        var tourists = touristRepository.findAll();
        log.info("Reindexing ac_term: {} tourists", tourists.size());

        // (선택) 전체 초기화가 필요하면 주석 해제
        // acTermRepo.deleteAllInBatch();

        final int BATCH = 500;
        int i = 0;
        List<AcTerm> bulk = new ArrayList<>(BATCH * 4);

        for (Tourist t : tourists) {
            bulk.addAll(buildTermsFor(t));
            if (++i % BATCH == 0) {
                saveBulk(bulk);
                bulk.clear();
                // 대량 처리 시 1차 캐시 정리
                em.flush();
                em.clear();
            }
        }
        saveBulk(bulk); // 잔여분
        log.info("Reindexing ac_term done");
    }

    /** ▶ 단일 관광지 변경 시 즉시 재색인 */
    @Transactional
    public void reindexTourist(Tourist t) {
        String refId = String.valueOf(t.getId());
        acTermRepo.deleteByTypeAndRefId("tourist", refId);
        var terms = dedupe(buildTermsFor(t));
        if (!terms.isEmpty()) acTermRepo.saveAll(terms);
    }

    /* ------------ 내부 헬퍼 ------------ */

    /** Tourist 1건을 ac_term 레코드 리스트로 전개 (이름>설명>주소>카테고리 한 번에) */
    private List<AcTerm> buildTermsFor(Tourist t) {
        String refId = String.valueOf(t.getId());
        List<AcTerm> out = new ArrayList<>(8);

        // 1) 이름 (1순위)
        addIfNotBlank(out, t.getName(), "tourist", refId, TermWeights.NAME);

        // 2) 설명 키워드 (2순위)
        if (t.getDescription() != null) {
            for (String kw : lexicon.extractFrom(t.getDescription())) {
                addIfNotBlank(out, kw, "keyword", refId, TermWeights.DESC);
            }
        }

        // 3) 주소 토큰 (3순위)
        if (t.getAddress() != null) {
            for (String token : addressSplitter.splitRegionTokens(t.getAddress())) {
                addIfNotBlank(out, token, "region", refId, TermWeights.ADDRESS);
            }
        }
        return out;
    }

    private void addIfNotBlank(List<AcTerm> sink, String term, String type, String refId, int weight) {
        if (term == null) return;
        String trimmed = term.trim();
        if (trimmed.isEmpty()) return;
        if (trimmed.length() > 200) trimmed = trimmed.substring(0, 200);
        sink.add(AcTerm.builder()
                .term(trimmed)
                .type(type)
                .refId(refId)
                .weight(weight)
                .build());
    }

    private List<AcTerm> dedupe(List<AcTerm> list) {
        if (list.isEmpty()) return list;
        Set<String> seen = new HashSet<>();
        List<AcTerm> out = new ArrayList<>(list.size());
        for (AcTerm t : list) {
            String k = t.getType() + "|" + t.getRefId() + "|" + t.getTerm();
            if (seen.add(k)) out.add(t);
        }
        return out;
    }

    private void saveBulk(List<AcTerm> bulk) {
        if (!bulk.isEmpty()) acTermRepo.saveAll(bulk);
    }
}
