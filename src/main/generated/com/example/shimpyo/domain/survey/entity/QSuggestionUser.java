package com.example.shimpyo.domain.survey.entity;

import static com.querydsl.core.types.PathMetadataFactory.*;

import com.querydsl.core.types.dsl.*;

import com.querydsl.core.types.PathMetadata;
import javax.annotation.processing.Generated;
import com.querydsl.core.types.Path;
import com.querydsl.core.types.dsl.PathInits;


/**
 * QSuggestionUser is a Querydsl query type for SuggestionUser
 */
@Generated("com.querydsl.codegen.DefaultEntitySerializer")
public class QSuggestionUser extends EntityPathBase<SuggestionUser> {

    private static final long serialVersionUID = 614703518L;

    private static final PathInits INITS = PathInits.DIRECT2;

    public static final QSuggestionUser suggestionUser = new QSuggestionUser("suggestionUser");

    public final com.example.shimpyo.domain.common.QBaseEntity _super = new com.example.shimpyo.domain.common.QBaseEntity(this);

    //inherited
    public final DateTimePath<java.time.LocalDateTime> createdAt = _super.createdAt;

    //inherited
    public final DateTimePath<java.time.LocalDateTime> deletedAt = _super.deletedAt;

    public final NumberPath<Long> id = createNumber("id", Long.class);

    public final QSuggestion suggestion;

    //inherited
    public final DateTimePath<java.time.LocalDateTime> updatedAt = _super.updatedAt;

    public final com.example.shimpyo.domain.user.entity.QUser user;

    public QSuggestionUser(String variable) {
        this(SuggestionUser.class, forVariable(variable), INITS);
    }

    public QSuggestionUser(Path<? extends SuggestionUser> path) {
        this(path.getType(), path.getMetadata(), PathInits.getFor(path.getMetadata(), INITS));
    }

    public QSuggestionUser(PathMetadata metadata) {
        this(metadata, PathInits.getFor(metadata, INITS));
    }

    public QSuggestionUser(PathMetadata metadata, PathInits inits) {
        this(SuggestionUser.class, metadata, inits);
    }

    public QSuggestionUser(Class<? extends SuggestionUser> type, PathMetadata metadata, PathInits inits) {
        super(type, metadata, inits);
        this.suggestion = inits.isInitialized("suggestion") ? new QSuggestion(forProperty("suggestion"), inits.get("suggestion")) : null;
        this.user = inits.isInitialized("user") ? new com.example.shimpyo.domain.user.entity.QUser(forProperty("user"), inits.get("user")) : null;
    }

}

