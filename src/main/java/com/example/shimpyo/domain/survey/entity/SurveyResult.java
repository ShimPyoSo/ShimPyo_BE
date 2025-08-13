package com.example.shimpyo.domain.survey.entity;

import com.example.shimpyo.domain.common.BaseEntity;
import com.example.shimpyo.domain.user.entity.User;
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
@SQLDelete(sql = "UPDATE survey_result SET deleted_at = now() WHERE id = ?")
// 조회 하는 경우 deleted_at 이 null 인 데이터만 조회
@SQLRestriction("deleted_at IS NULL")
@Table(name = "survey_result")
public class SurveyResult extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(optional = false) // 각 surveyResult는 반드시 하나의 User에 속해야 함.
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @OneToOne(mappedBy = "surveyResult", cascade = CascadeType.ALL, orphanRemoval = true)
    private Suggestion suggestion;

    public void setSuggestion(Suggestion suggestion) {
        this.suggestion = suggestion;
        if (suggestion.getSurveyResult() != this) {
            suggestion.setSurveyResult(this);
        }
    }
}
