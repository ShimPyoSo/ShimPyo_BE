package com.example.shimpyo.domain.survey.entity;

import static com.querydsl.core.types.PathMetadataFactory.*;

import com.querydsl.core.types.dsl.*;

import com.querydsl.core.types.PathMetadata;
import javax.annotation.processing.Generated;
import com.querydsl.core.types.Path;
import com.querydsl.core.types.dsl.PathInits;


/**
 * QSuggestionTourist is a Querydsl query type for SuggestionTourist
 */
@Generated("com.querydsl.codegen.DefaultEntitySerializer")
public class QSuggestionTourist extends EntityPathBase<SuggestionTourist> {

    private static final long serialVersionUID = -2095184769L;

    private static final PathInits INITS = PathInits.DIRECT2;

    public static final QSuggestionTourist suggestionTourist = new QSuggestionTourist("suggestionTourist");

    public final com.example.shimpyo.domain.common.QBaseEntity _super = new com.example.shimpyo.domain.common.QBaseEntity(this);

    //inherited
    public final DateTimePath<java.time.LocalDateTime> createdAt = _super.createdAt;

    public final StringPath date = createString("date");

    //inherited
    public final DateTimePath<java.time.LocalDateTime> deletedAt = _super.deletedAt;

    public final NumberPath<Long> id = createNumber("id", Long.class);

    public final QSuggestion suggestion;

    public final TimePath<java.time.LocalTime> time = createTime("time", java.time.LocalTime.class);

    public final com.example.shimpyo.domain.tourist.entity.QTourist tourist;

    //inherited
    public final DateTimePath<java.time.LocalDateTime> updatedAt = _super.updatedAt;

    public QSuggestionTourist(String variable) {
        this(SuggestionTourist.class, forVariable(variable), INITS);
    }

    public QSuggestionTourist(Path<? extends SuggestionTourist> path) {
        this(path.getType(), path.getMetadata(), PathInits.getFor(path.getMetadata(), INITS));
    }

    public QSuggestionTourist(PathMetadata metadata) {
        this(metadata, PathInits.getFor(metadata, INITS));
    }

    public QSuggestionTourist(PathMetadata metadata, PathInits inits) {
        this(SuggestionTourist.class, metadata, inits);
    }

    public QSuggestionTourist(Class<? extends SuggestionTourist> type, PathMetadata metadata, PathInits inits) {
        super(type, metadata, inits);
        this.suggestion = inits.isInitialized("suggestion") ? new QSuggestion(forProperty("suggestion"), inits.get("suggestion")) : null;
        this.tourist = inits.isInitialized("tourist") ? new com.example.shimpyo.domain.tourist.entity.QTourist(forProperty("tourist")) : null;
    }

}

