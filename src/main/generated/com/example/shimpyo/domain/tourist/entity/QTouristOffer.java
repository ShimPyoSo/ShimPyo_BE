package com.example.shimpyo.domain.tourist.entity;

import static com.querydsl.core.types.PathMetadataFactory.*;

import com.querydsl.core.types.dsl.*;

import com.querydsl.core.types.PathMetadata;
import javax.annotation.processing.Generated;
import com.querydsl.core.types.Path;
import com.querydsl.core.types.dsl.PathInits;


/**
 * QTouristOffer is a Querydsl query type for TouristOffer
 */
@Generated("com.querydsl.codegen.DefaultEntitySerializer")
public class QTouristOffer extends EntityPathBase<TouristOffer> {

    private static final long serialVersionUID = 854446881L;

    private static final PathInits INITS = PathInits.DIRECT2;

    public static final QTouristOffer touristOffer = new QTouristOffer("touristOffer");

    public final com.example.shimpyo.domain.common.QBaseEntity _super = new com.example.shimpyo.domain.common.QBaseEntity(this);

    //inherited
    public final DateTimePath<java.time.LocalDateTime> createdAt = _super.createdAt;

    //inherited
    public final DateTimePath<java.time.LocalDateTime> deletedAt = _super.deletedAt;

    public final NumberPath<Long> id = createNumber("id", Long.class);

    public final EnumPath<Offer> offer = createEnum("offer", Offer.class);

    public final QTourist tourist;

    //inherited
    public final DateTimePath<java.time.LocalDateTime> updatedAt = _super.updatedAt;

    public QTouristOffer(String variable) {
        this(TouristOffer.class, forVariable(variable), INITS);
    }

    public QTouristOffer(Path<? extends TouristOffer> path) {
        this(path.getType(), path.getMetadata(), PathInits.getFor(path.getMetadata(), INITS));
    }

    public QTouristOffer(PathMetadata metadata) {
        this(metadata, PathInits.getFor(metadata, INITS));
    }

    public QTouristOffer(PathMetadata metadata, PathInits inits) {
        this(TouristOffer.class, metadata, inits);
    }

    public QTouristOffer(Class<? extends TouristOffer> type, PathMetadata metadata, PathInits inits) {
        super(type, metadata, inits);
        this.tourist = inits.isInitialized("tourist") ? new QTourist(forProperty("tourist")) : null;
    }

}

