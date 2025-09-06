package com.example.shimpyo.domain.survey.entity;

import com.example.shimpyo.domain.common.BaseEntity;
import com.example.shimpyo.domain.tourist.entity.Tourist;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.SQLDelete;
import org.hibernate.annotations.SQLRestriction;

import java.time.LocalTime;

@NoArgsConstructor
@AllArgsConstructor
@Entity
@Builder
@Getter
// Hibernate 구현체가 delete를 수행하는 경우 soft delete 로 수행하도록 하는 명령어
@SQLDelete(sql = "UPDATE suggestion_tourist SET deleted_at = now() WHERE id = ?")
// 조회 하는 경우 deleted_at 이 null 인 데이터만 조회
@SQLRestriction("deleted_at IS NULL")
public class SuggestionTourist extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotNull
    @Column
    private String date;

    @NotNull
    @Column
    private LocalTime time;

    @ManyToOne(optional = false)
    @JoinColumn(name = "suggestion_id", nullable = false)
    private Suggestion suggestion;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "tourist_id")
    private Tourist tourist;

    public void defineDate(String date) {
        this.date = date;
    }
    public void defineTime(LocalTime time) {
        this.time = time;
    }
    public void addSuggestion(Suggestion suggestion) {
        this.suggestion = suggestion;
        suggestion.getSuggestionTourists().add(this);
    }

    public void setTourist(Tourist tourist) {
        this.tourist = tourist;
        tourist.getSuggestionTourists().add(this);
    }
}
