package com.example.shimpyo.domain.tourist.entity;

import static com.querydsl.core.types.PathMetadataFactory.*;

import com.querydsl.core.types.dsl.*;

import com.querydsl.core.types.PathMetadata;
import javax.annotation.processing.Generated;
import com.querydsl.core.types.Path;


/**
 * QAbstractTourist is a Querydsl query type for AbstractTourist
 */
@Generated("com.querydsl.codegen.DefaultSupertypeSerializer")
public class QAbstractTourist extends EntityPathBase<AbstractTourist> {

    private static final long serialVersionUID = 1252810873L;

    public static final QAbstractTourist abstractTourist = new QAbstractTourist("abstractTourist");

    public final StringPath address = createString("address");

    public final DateTimePath<java.time.LocalDateTime> createdAt = createDateTime("createdAt", java.time.LocalDateTime.class);

    public final DateTimePath<java.time.LocalDateTime> deletedAt = createDateTime("deletedAt", java.time.LocalDateTime.class);

    public final NumberPath<Long> id = createNumber("id", Long.class);

    public final StringPath image = createString("image");

    public final NumberPath<Double> latitude = createNumber("latitude", Double.class);

    public final NumberPath<Double> longitude = createNumber("longitude", Double.class);

    public final StringPath name = createString("name");

    public final StringPath region = createString("region");

    public final StringPath regionDetail = createString("regionDetail");

    public final StringPath tel = createString("tel");

    public final DateTimePath<java.time.LocalDateTime> updatedAt = createDateTime("updatedAt", java.time.LocalDateTime.class);

    public QAbstractTourist(String variable) {
        super(AbstractTourist.class, forVariable(variable));
    }

    public QAbstractTourist(Path<? extends AbstractTourist> path) {
        super(path.getType(), path.getMetadata());
    }

    public QAbstractTourist(PathMetadata metadata) {
        super(AbstractTourist.class, metadata);
    }

}

