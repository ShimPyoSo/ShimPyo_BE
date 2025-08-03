package com.example.shimpyo.domain.tourist.entity;

import com.example.shimpyo.domain.common.BaseEntity;
import com.example.shimpyo.domain.course.entity.SuggestionTourist;
import com.example.shimpyo.domain.course.entity.UserCourseDetail;
import com.example.shimpyo.domain.course.entity.UserCourseTourist;
import com.example.shimpyo.domain.user.entity.Likes;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import net.minidev.json.annotate.JsonIgnore;
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
    private String image;

    @Column
    private float latitude;
    @Column
    private float longitude;

    @Column
    private String tel;

    @Column
    private Long content_id;

    @Column
    private String description;

    @Column
    private String operationTime;

    @Column
    private String homepageUrl;

    @Column
    private String reservationUrl;

    @Column
    private String dayOff;

    @Column
    private String breakTime;

    @OneToMany(mappedBy = "tourist", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<TouristCategory> touristCategories = new ArrayList<>();

    @OneToMany(mappedBy = "tourist", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<SuggestionTourist> suggestionTourists = new ArrayList<>();

    @OneToMany(mappedBy = "tourist", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<UserCourseTourist> userCourseTourists = new ArrayList<>();

    @OneToMany(mappedBy = "tourist", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Likes> likes = new ArrayList<>();
}


