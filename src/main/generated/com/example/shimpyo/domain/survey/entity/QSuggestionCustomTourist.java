package com.example.shimpyo.domain.survey.entity;

import static com.querydsl.core.types.PathMetadataFactory.*;

import com.querydsl.core.types.dsl.*;

import com.querydsl.core.types.PathMetadata;
import javax.annotation.processing.Generated;
import com.querydsl.core.types.Path;
import com.querydsl.core.types.dsl.PathInits;


/**
 * QSuggestionCustomTourist is a Querydsl query type for SuggestionCustomTourist
 */
@Generated("com.querydsl.codegen.DefaultEntitySerializer")
public class QSuggestionCustomTourist extends EntityPathBase<SuggestionCustomTourist> {

    private static final long serialVersionUID = 1402901006L;

    private static final PathInits INITS = PathInits.DIRECT2;

    public static final QSuggestionCustomTourist suggestionCustomTourist = new QSuggestionCustomTourist("suggestionCustomTourist");

    public final com.example.shimpyo.domain.common.QBaseEntity _super = new com.example.shimpyo.domain.common.QBaseEntity(this);

    //inherited
    public final DateTimePath<java.time.LocalDateTime> createdAt = _super.createdAt;

    public final com.example.shimpyo.domain.tourist.entity.QCustomTourist customTourist;

    public final StringPath date = createString("date");

    //inherited
    public final DateTimePath<java.time.LocalDateTime> deletedAt = _super.deletedAt;

    public final NumberPath<Long> id = createNumber("id", Long.class);

    public final QSuggestion suggestion;

    public final TimePath<java.time.LocalTime> time = createTime("time", java.time.LocalTime.class);

    //inherited
    public final DateTimePath<java.time.LocalDateTime> updatedAt = _super.updatedAt;

    public QSuggestionCustomTourist(String variable) {
        this(SuggestionCustomTourist.class, forVariable(variable), INITS);
    }

    public QSuggestionCustomTourist(Path<? extends SuggestionCustomTourist> path) {
        this(path.getType(), path.getMetadata(), PathInits.getFor(path.getMetadata(), INITS));
    }

    public QSuggestionCustomTourist(PathMetadata metadata) {
        this(metadata, PathInits.getFor(metadata, INITS));
    }

    public QSuggestionCustomTourist(PathMetadata metadata, PathInits inits) {
        this(SuggestionCustomTourist.class, metadata, inits);
    }

    public QSuggestionCustomTourist(Class<? extends SuggestionCustomTourist> type, PathMetadata metadata, PathInits inits) {
        super(type, metadata, inits);
        this.customTourist = inits.isInitialized("customTourist") ? new com.example.shimpyo.domain.tourist.entity.QCustomTourist(forProperty("customTourist")) : null;
        this.suggestion = inits.isInitialized("suggestion") ? new QSuggestion(forProperty("suggestion"), inits.get("suggestion")) : null;
    }

}

