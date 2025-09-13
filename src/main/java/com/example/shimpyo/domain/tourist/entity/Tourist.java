package com.example.shimpyo.domain.tourist.entity;

import com.example.shimpyo.domain.survey.entity.SuggestionTourist;
import com.example.shimpyo.domain.tourist.listeners.TouristEntityListener;
import com.example.shimpyo.domain.user.entity.Likes;
import com.example.shimpyo.domain.user.entity.Review;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Getter
@Table(name = "tourist")
@SuperBuilder
@NoArgsConstructor
@EntityListeners(TouristEntityListener.class)
public class Tourist extends AbstractTourist {

    @Column
    private Long contentId;

    @Column
    private String homepageUrl;

    @Column
    private String reservationUrl;

    @Column
    private String dayOffShow;

    @Column
    private String dayOffCal;

    @Column
    private LocalTime openTime;

    @Column
    private LocalTime closeTime;

    @Column
    private String breakTime;

    @Column
    private Double maleRatio;

    @Column
    private Double femaleRatio;

    @Column(name = "age20_early_ratio")   // 20대 초반
    private Double age20EarlyRatio;
    @Column(name = "age20_mid_ratio")     // 20대 중반
    private Double age20MidRatio;
    @Column(name = "age20_late_ratio")    // 20대 후반
    private Double age20LateRatio;
    @Column(name = "age30_early_ratio")   // 30대 초반
    private Double age30EarlyRatio;
    @Column(name = "age30_mid_ratio")     // 30대 중반
    private Double age30MidRatio;
    @Column(name = "age30_late_ratio")    // 30대 후반
    private Double age30LateRatio;
    @Column(name = "age40_ratio")         // 40대 (단일)
    private Double age40Ratio;
    @Column(name = "age50_ratio")         // 50대 (단일)
    private Double age50Ratio;
    @Column(name = "age60plus_ratio")     // 60대 이상
    private Double age60PlusRatio;

    @OneToMany(mappedBy = "tourist", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<TouristCategory> touristCategories = new ArrayList<>();

    @OneToMany(mappedBy = "tourist", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<TouristOffer> touristOffers = new ArrayList<>();

    @OneToMany(mappedBy = "tourist", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<SuggestionTourist> suggestionTourists = new ArrayList<>();

    @OneToMany(mappedBy = "tourist", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Likes> likes = new ArrayList<>();

    @OneToMany(mappedBy = "tourist", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Review> reviews = new ArrayList<>();

    public void addSuggestionTourist(SuggestionTourist st) {
        suggestionTourists.add(st);
        if (st.getTourist() != this) {
            st.addTourist(this);
        }
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getId() {
        return id;
    }
}


