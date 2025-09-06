package com.example.shimpyo.domain.tourist.entity;
import co.elastic.clients.elasticsearch.xpack.usage.Base;
import com.example.shimpyo.domain.common.BaseEntity;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.SQLDelete;
import org.hibernate.annotations.UpdateTimestamp;
import org.hibernate.annotations.Where;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;

@Getter
@MappedSuperclass
@SQLDelete(sql = "UPDATE abstract_tourist SET deleted_at = now() WHERE id = ?")
@Where(clause = "deleted_at IS NULL")
@NoArgsConstructor
@SuperBuilder
@EntityListeners(AuditingEntityListener.class)
public abstract class AbstractTourist {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    protected Long id;

    @Column(nullable = false)
    protected String name;

    @Column
    protected String address;

    @Column
    protected String image;

    @Column
    private String region;

    @Column
    protected Double latitude;

    @Column
    protected Double longitude;

    @Column
    protected String tel;

    @Column(updatable = false)
    @CreationTimestamp
    private LocalDateTime createdAt;

    @UpdateTimestamp
    private LocalDateTime updatedAt;

    private LocalDateTime deletedAt;

}
