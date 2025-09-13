package com.example.shimpyo.domain.tourist.listeners;

import com.example.shimpyo.domain.tourist.entity.Tourist;
import jakarta.persistence.PostPersist;
import jakarta.persistence.PostRemove;
import jakarta.persistence.PostUpdate;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class TouristEntityListener {

    private static ApplicationEventPublisher publisher;

    @Autowired
    public void init(ApplicationEventPublisher applicationEventPublisher) {
        publisher = applicationEventPublisher;
    }

    @PostPersist
    @PostUpdate
    public void afterSave(Tourist tourist) {
        publisher.publishEvent(new TouristSavedEvent(tourist));
    }

    @PostRemove
    public void afterDelete(Tourist tourist) {
        publisher.publishEvent(new TouristDeletedEvent(tourist.getId()));
    }

    @Getter
    @AllArgsConstructor
    public static class TouristSavedEvent {
        private final Tourist tourist;
    }

    @Getter
    @AllArgsConstructor
    public static class TouristDeletedEvent {
        private final Long touristId;
    }
}
