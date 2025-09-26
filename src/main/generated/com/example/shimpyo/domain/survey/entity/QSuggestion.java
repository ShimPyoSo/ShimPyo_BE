package com.example.shimpyo.domain.survey.entity;

import static com.querydsl.core.types.PathMetadataFactory.*;

import com.querydsl.core.types.dsl.*;

import com.querydsl.core.types.PathMetadata;
import javax.annotation.processing.Generated;
import com.querydsl.core.types.Path;
import com.querydsl.core.types.dsl.PathInits;


/**
 * QSuggestion is a Querydsl query type for Suggestion
 */
@Generated("com.querydsl.codegen.DefaultEntitySerializer")
public class QSuggestion extends EntityPathBase<Suggestion> {

    private static final long serialVersionUID = 1022235443L;

    private static final PathInits INITS = PathInits.DIRECT2;

    public static final QSuggestion suggestion = new QSuggestion("suggestion");

    public final com.example.shimpyo.domain.common.QBaseEntity _super = new com.example.shimpyo.domain.common.QBaseEntity(this);

    //inherited
    public final DateTimePath<java.time.LocalDateTime> createdAt = _super.createdAt;

    //inherited
    public final DateTimePath<java.time.LocalDateTime> deletedAt = _super.deletedAt;

    public final StringPath duration = createString("duration");

    public final NumberPath<Long> id = createNumber("id", Long.class);

    public final ListPath<SuggestionCustomTourist, QSuggestionCustomTourist> suggestionCustomTourists = this.<SuggestionCustomTourist, QSuggestionCustomTourist>createList("suggestionCustomTourists", SuggestionCustomTourist.class, QSuggestionCustomTourist.class, PathInits.DIRECT2);

    public final ListPath<SuggestionTourist, QSuggestionTourist> suggestionTourists = this.<SuggestionTourist, QSuggestionTourist>createList("suggestionTourists", SuggestionTourist.class, QSuggestionTourist.class, PathInits.DIRECT2);

    public final StringPath title = createString("title");

    public final StringPath token = createString("token");

    //inherited
    public final DateTimePath<java.time.LocalDateTime> updatedAt = _super.updatedAt;

    public final com.example.shimpyo.domain.user.entity.QUser user;

    public final EnumPath<WellnessType> wellnessType = createEnum("wellnessType", WellnessType.class);

    public QSuggestion(String variable) {
        this(Suggestion.class, forVariable(variable), INITS);
    }

    public QSuggestion(Path<? extends Suggestion> path) {
        this(path.getType(), path.getMetadata(), PathInits.getFor(path.getMetadata(), INITS));
    }

    public QSuggestion(PathMetadata metadata) {
        this(metadata, PathInits.getFor(metadata, INITS));
    }

    public QSuggestion(PathMetadata metadata, PathInits inits) {
        this(Suggestion.class, metadata, inits);
    }

    public QSuggestion(Class<? extends Suggestion> type, PathMetadata metadata, PathInits inits) {
        super(type, metadata, inits);
        this.user = inits.isInitialized("user") ? new com.example.shimpyo.domain.user.entity.QUser(forProperty("user"), inits.get("user")) : null;
    }

}

