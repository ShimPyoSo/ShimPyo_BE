package com.example.shimpyo.domain.tourist.entity;

import static com.querydsl.core.types.PathMetadataFactory.*;

import com.querydsl.core.types.dsl.*;

import com.querydsl.core.types.PathMetadata;
import javax.annotation.processing.Generated;
import com.querydsl.core.types.Path;
import com.querydsl.core.types.dsl.PathInits;


/**
 * QCustomTourist is a Querydsl query type for CustomTourist
 */
@Generated("com.querydsl.codegen.DefaultEntitySerializer")
public class QCustomTourist extends EntityPathBase<CustomTourist> {

    private static final long serialVersionUID = -435381110L;

    public static final QCustomTourist customTourist = new QCustomTourist("customTourist");

    public final QAbstractTourist _super = new QAbstractTourist(this);

    //inherited
    public final StringPath address = _super.address;

    //inherited
    public final DateTimePath<java.time.LocalDateTime> createdAt = _super.createdAt;

    //inherited
    public final DateTimePath<java.time.LocalDateTime> deletedAt = _super.deletedAt;

    //inherited
    public final NumberPath<Long> id = _super.id;

    //inherited
    public final StringPath image = _super.image;

    //inherited
    public final NumberPath<Double> latitude = _super.latitude;

    //inherited
    public final NumberPath<Double> longitude = _super.longitude;

    //inherited
    public final StringPath name = _super.name;

    //inherited
    public final StringPath region = _super.region;

    public final ListPath<com.example.shimpyo.domain.survey.entity.SuggestionCustomTourist, com.example.shimpyo.domain.survey.entity.QSuggestionCustomTourist> suggestionCustomTourists = this.<com.example.shimpyo.domain.survey.entity.SuggestionCustomTourist, com.example.shimpyo.domain.survey.entity.QSuggestionCustomTourist>createList("suggestionCustomTourists", com.example.shimpyo.domain.survey.entity.SuggestionCustomTourist.class, com.example.shimpyo.domain.survey.entity.QSuggestionCustomTourist.class, PathInits.DIRECT2);

    //inherited
    public final StringPath tel = _super.tel;

    //inherited
    public final DateTimePath<java.time.LocalDateTime> updatedAt = _super.updatedAt;

    public QCustomTourist(String variable) {
        super(CustomTourist.class, forVariable(variable));
    }

    public QCustomTourist(Path<? extends CustomTourist> path) {
        super(path.getType(), path.getMetadata());
    }

    public QCustomTourist(PathMetadata metadata) {
        super(CustomTourist.class, metadata);
    }

}

