package com.example.shimpyo.domain.course.entity;

import com.example.shimpyo.domain.common.BaseEntity;
import com.example.shimpyo.domain.user.entity.User;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
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
@SQLDelete(sql = "UPDATE user_course SET deleted_at = now() WHERE id = ?")
// 조회 하는 경우 deleted_at 이 null 인 데이터만 조회
@SQLRestriction("deleted_at IS NULL")
public class UserCourse extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column
    @NotNull
    private String title;

    @Column
    private String description;

    @ManyToOne()
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @OneToMany(mappedBy = "userCourse", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<UserCourseList> userCourseLists = new ArrayList<>();
}
