package com.example.shimpyo.domain.user.entity;

import com.example.shimpyo.domain.common.BaseEntity;
import com.example.shimpyo.domain.tourist.entity.Tourist;
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
@SQLDelete(sql = "UPDATE review SET deleted_at = now() WHERE id = ?")
@SQLRestriction("deleted_at IS NULL")
public class Review extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String content;

    @Column
    // 이미지 여러개 넣는 경우가 있을 수 있으니 List 로 설정함
    private List<String> image = new ArrayList<>();

    @ManyToOne
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @ManyToOne
    @JoinColumn(name = "tourist_id", nullable = false)
    private Tourist tourist;

}
