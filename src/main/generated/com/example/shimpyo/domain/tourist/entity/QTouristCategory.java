package com.example.shimpyo.domain.tourist.entity;

import static com.querydsl.core.types.PathMetadataFactory.*;

import com.querydsl.core.types.dsl.*;

import com.querydsl.core.types.PathMetadata;
import javax.annotation.processing.Generated;
import com.querydsl.core.types.Path;
import com.querydsl.core.types.dsl.PathInits;


/**
 * QTouristCategory is a Querydsl query type for TouristCategory
 */
@Generated("com.querydsl.codegen.DefaultEntitySerializer")
public class QTouristCategory extends EntityPathBase<TouristCategory> {

    private static final long serialVersionUID = -624979431L;

    private static final PathInits INITS = PathInits.DIRECT2;

    public static final QTouristCategory touristCategory = new QTouristCategory("touristCategory");

    public final com.example.shimpyo.domain.common.QBaseEntity _super = new com.example.shimpyo.domain.common.QBaseEntity(this);

    public final EnumPath<Category> category = createEnum("category", Category.class);

    //inherited
    public final DateTimePath<java.time.LocalDateTime> createdAt = _super.createdAt;

    //inherited
    public final DateTimePath<java.time.LocalDateTime> deletedAt = _super.deletedAt;

    public final NumberPath<Long> id = createNumber("id", Long.class);

    public final QTourist tourist;

    //inherited
    public final DateTimePath<java.time.LocalDateTime> updatedAt = _super.updatedAt;

    public QTouristCategory(String variable) {
        this(TouristCategory.class, forVariable(variable), INITS);
    }

    public QTouristCategory(Path<? extends TouristCategory> path) {
        this(path.getType(), path.getMetadata(), PathInits.getFor(path.getMetadata(), INITS));
    }

    public QTouristCategory(PathMetadata metadata) {
        this(metadata, PathInits.getFor(metadata, INITS));
    }

    public QTouristCategory(PathMetadata metadata, PathInits inits) {
        this(TouristCategory.class, metadata, inits);
    }

    public QTouristCategory(Class<? extends TouristCategory> type, PathMetadata metadata, PathInits inits) {
        super(type, metadata, inits);
        this.tourist = inits.isInitialized("tourist") ? new QTourist(forProperty("tourist")) : null;
    }

}

