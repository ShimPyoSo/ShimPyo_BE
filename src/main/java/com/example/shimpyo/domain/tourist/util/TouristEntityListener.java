package com.example.shimpyo.domain.tourist.util;

import com.example.shimpyo.domain.search.entity.AcTerm;
import com.example.shimpyo.domain.search.repository.AcTermRepository;
import com.example.shimpyo.domain.tourist.entity.Tourist;
import jakarta.persistence.PostPersist;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

@Component
@RequiredArgsConstructor
public class TouristEntityListener {

    private static AcTermRepository acTermRepository;

    @Autowired
    public void init(AcTermRepository repository) {
        TouristEntityListener.acTermRepository = repository;
    }

    @PostPersist
    public void createAcTerms(Tourist t) {
        List<AcTerm> acTerms = new ArrayList<>();

        if (t.getName() != null && !t.getName().isBlank())
            acTerms.add(AcTerm.builder().term(t.getName()).build());
        if (t.getRegion() != null && !t.getRegion().isBlank())
            acTerms.add(AcTerm.builder().term(t.getRegion()).build());
        if (t.getAddress() != null && !t.getAddress().isBlank())
            acTerms.add(AcTerm.builder().term(t.getAddress()).build());
        if (t.getDescription() != null && !t.getDescription().isBlank())
            acTerms.add(AcTerm.builder().term(t.getDescription()).build());

        acTermRepository.saveAll(acTerms);
    }
}
