package com.example.shimpyo.domain.course.entity;

import com.example.shimpyo.domain.common.BaseEntity;
import com.example.shimpyo.domain.tourist.entity.Tourist;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.SQLDelete;
import org.hibernate.annotations.SQLRestriction;

@NoArgsConstructor
@AllArgsConstructor
@Entity
@Builder
@Getter
// Hibernate 구현체가 delete를 수행하는 경우 soft delete 로 수행하도록 하는 명령어
@SQLDelete(sql = "UPDATE user_course_list SET deleted_at = now() WHERE id = ?")
// 조회 하는 경우 deleted_at 이 null 인 데이터만 조회
@SQLRestriction("deleted_at IS NULL")
public class UserCourseList extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "user_course_id", nullable = false)
    private UserCourse userCourse;

    @ManyToOne
    @JoinColumn(name = "tourist_id", nullable = false)
    private Tourist tourist;

}
