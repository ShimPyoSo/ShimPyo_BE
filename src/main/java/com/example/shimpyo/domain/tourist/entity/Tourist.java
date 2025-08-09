package com.example.shimpyo.domain.tourist.entity;

import com.example.shimpyo.domain.common.BaseEntity;
import com.example.shimpyo.domain.course.entity.SuggestionTourist;
import com.example.shimpyo.domain.course.entity.UserCourseList;
import com.example.shimpyo.domain.user.entity.Likes;
import com.example.shimpyo.domain.user.entity.Review;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.SQLDelete;
import org.hibernate.annotations.SQLRestriction;

import java.util.ArrayList;
import java.util.List;

@NoArgsConstructor
@AllArgsConstructor
@Entity
@Builder
@Getter
// Hibernate 구현체가 delete를 수행하는 경우 soft delete 로 수행하도록 하는 명령어
@SQLDelete(sql = "UPDATE tourist SET deleted_at = now() WHERE id = ?")
// 조회 하는 경우 deleted_at 이 null 인 데이터만 조회
@SQLRestriction("deleted_at IS NULL")
public class Tourist extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column
    private String name;

    @Column
    private String address;

    @Column
    private String region;

    @Column
    private String image;

    @Column
    private float latitude;
    @Column
    private float longitude;

    @Column
    private Long content_id;

    @Column
    private String description;

    @Column
    private String homepageUrl;

    @Column
    private String reservationUrl;

    @Column
    private String dayOff;

    // 오픈 시간
    @Column
    private String openTime;

    // 마감 시간
    @Column
    private String closeTime;

    // 휴계시간
    @Column
    private String breakTime;
    // 전화번호
    @Column
    private String telNum;
    // 제공 서비스
    @Column
    private String requiredService;

    @Column
    private Double maleRatio;

    @Column
    private Double femaleRatio;

    @Column(name = "age20_early_ratio")   // 20대 초반
    private double age20EarlyRatio;
    @Column(name = "age20_mid_ratio")     // 20대 중반
    private double age20MidRatio;
    @Column(name = "age20_late_ratio")    // 20대 후반
    private double age20LateRatio;
    @Column(name = "age30_early_ratio")   // 30대 초반
    private double age30EarlyRatio;
    @Column(name = "age30_mid_ratio")     // 30대 중반
    private double age30MidRatio;
    @Column(name = "age30_late_ratio")    // 30대 후반
    private double age30LateRatio;
    @Column(name = "age40_ratio")         // 40대 (단일)
    private double age40Ratio;
    @Column(name = "age50_ratio")         // 50대 (단일)
    private double age50Ratio;
    @Column(name = "age60plus_ratio")     // 60대 이상
    private double age60PlusRatio;

    @OneToMany(mappedBy = "tourist", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<TouristCategory> touristCategories = new ArrayList<>();

    @OneToMany(mappedBy = "tourist", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<SuggestionTourist> suggestionTourists = new ArrayList<>();

    @OneToMany(mappedBy = "tourist", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<UserCourseList> userCourseLists = new ArrayList<>();

    @OneToMany(mappedBy = "tourist", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Likes> likes = new ArrayList<>();

    @OneToMany(mappedBy = "tourist", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Review> reviews = new ArrayList<>();
}


