package com.example.shimpyo.domain.user.entity;

import com.example.shimpyo.domain.auth.entity.UserAuth;
import com.example.shimpyo.domain.common.BaseEntity;
import com.example.shimpyo.domain.course.entity.Suggestion;
import com.example.shimpyo.domain.course.entity.UserCourse;
import com.example.shimpyo.domain.survey.entity.SurveyResult;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.SQLDelete;
import org.hibernate.annotations.SQLRestriction;
import jakarta.validation.constraints.NotNull;

import java.util.ArrayList;
import java.util.List;

@NoArgsConstructor
@AllArgsConstructor
@Entity
@Builder
@Getter
/** JPA 의 delete 문이 실행되는 순간 애가 그 요청을 받아와 다음과 같이 설정함
    deleted_at = 삭제 시각
    email = uuid -> not null 이기 때문에
    nickname = uuid -> not null 이므로
 **/
@SQLDelete(sql = "UPDATE `user` SET deleted_at = now(), email = UUID(), nickname = UUID() WHERE id = ?")
@SQLRestriction("deleted_at IS NULL")
@Table(name = "\"user\"")
public class User extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique = true, nullable = false)
    private String email;

    @Column(unique = true, nullable = false)
    private String nickname;

    @Column
    private String gender;

    @Column
    private Integer birthYear;

    // 이거 왜 있을까요??
    private Long survey;

    @OneToOne(mappedBy = "user", cascade = CascadeType.ALL, orphanRemoval = true)
    private UserAuth userAuth;

    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<SurveyResult> surveyResults = new ArrayList<>();

    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Suggestion> suggestions = new ArrayList<>();

    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<UserCourse> userCourses = new ArrayList<>();

    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Likes> likes = new ArrayList<>();

    public void changeNickname(String nickname) {
        this.nickname = nickname;
    }

    public void changeGender(String gender) {
        this.gender = gender;
    }
    public void changeBirthYear(Integer year) {
        this.birthYear = year;
    }
}
