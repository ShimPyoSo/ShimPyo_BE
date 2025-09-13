package com.example.shimpyo.domain.tourist.listeners;

import com.example.shimpyo.domain.tourist.entity.Tourist;
import com.example.shimpyo.domain.tourist.entity.TouristDocument;
import com.example.shimpyo.domain.tourist.repository.TouristElasticSearchRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionalEventListener;

@Component
@RequiredArgsConstructor
public class TouristEventHandler {

    private final TouristElasticSearchRepository repository;

    @Async
    @TransactionalEventListener
    public void handleTouristSaved(TouristEntityListener.TouristSavedEvent event) {
        Tourist tourist = event.getTourist();

        TouristDocument doc = TouristDocument.builder()
                .id(String.valueOf(tourist.getId()))
                .name(tourist.getName())
                .region(tourist.getRegion())
                .build();

        repository.save(doc);
    }

    @Async
    @TransactionalEventListener
    public void handleTouristDeleted(TouristEntityListener.TouristDeletedEvent event) {
        repository.deleteById(String.valueOf(event.getTouristId()));
    }
}
