package com.example.shimpyo.domain.tourist.entity;

import static com.querydsl.core.types.PathMetadataFactory.*;

import com.querydsl.core.types.dsl.*;

import com.querydsl.core.types.PathMetadata;
import javax.annotation.processing.Generated;
import com.querydsl.core.types.Path;
import com.querydsl.core.types.dsl.PathInits;


/**
 * QTourist is a Querydsl query type for Tourist
 */
@Generated("com.querydsl.codegen.DefaultEntitySerializer")
public class QTourist extends EntityPathBase<Tourist> {

    private static final long serialVersionUID = -644572165L;

    public static final QTourist tourist = new QTourist("tourist");

    public final QAbstractTourist _super = new QAbstractTourist(this);

    //inherited
    public final StringPath address = _super.address;

    public final NumberPath<Double> age20EarlyRatio = createNumber("age20EarlyRatio", Double.class);

    public final NumberPath<Double> age20LateRatio = createNumber("age20LateRatio", Double.class);

    public final NumberPath<Double> age20MidRatio = createNumber("age20MidRatio", Double.class);

    public final NumberPath<Double> age30EarlyRatio = createNumber("age30EarlyRatio", Double.class);

    public final NumberPath<Double> age30LateRatio = createNumber("age30LateRatio", Double.class);

    public final NumberPath<Double> age30MidRatio = createNumber("age30MidRatio", Double.class);

    public final NumberPath<Double> age40Ratio = createNumber("age40Ratio", Double.class);

    public final NumberPath<Double> age50Ratio = createNumber("age50Ratio", Double.class);

    public final NumberPath<Double> age60PlusRatio = createNumber("age60PlusRatio", Double.class);

    public final StringPath breakTime = createString("breakTime");

    public final TimePath<java.time.LocalTime> closeTime = createTime("closeTime", java.time.LocalTime.class);

    public final NumberPath<Long> contentId = createNumber("contentId", Long.class);

    //inherited
    public final DateTimePath<java.time.LocalDateTime> createdAt = _super.createdAt;

    public final StringPath dayOffCal = createString("dayOffCal");

    public final StringPath dayOffShow = createString("dayOffShow");

    //inherited
    public final DateTimePath<java.time.LocalDateTime> deletedAt = _super.deletedAt;

    public final NumberPath<Double> femaleRatio = createNumber("femaleRatio", Double.class);

    public final StringPath homepageUrl = createString("homepageUrl");

    //inherited
    public final NumberPath<Long> id = _super.id;

    //inherited
    public final StringPath image = _super.image;

    //inherited
    public final NumberPath<Double> latitude = _super.latitude;

    public final ListPath<com.example.shimpyo.domain.user.entity.Likes, com.example.shimpyo.domain.user.entity.QLikes> likes = this.<com.example.shimpyo.domain.user.entity.Likes, com.example.shimpyo.domain.user.entity.QLikes>createList("likes", com.example.shimpyo.domain.user.entity.Likes.class, com.example.shimpyo.domain.user.entity.QLikes.class, PathInits.DIRECT2);

    //inherited
    public final NumberPath<Double> longitude = _super.longitude;

    public final NumberPath<Double> maleRatio = createNumber("maleRatio", Double.class);

    //inherited
    public final StringPath name = _super.name;

    public final TimePath<java.time.LocalTime> openTime = createTime("openTime", java.time.LocalTime.class);

    //inherited
    public final StringPath region = _super.region;

    public final StringPath reservationUrl = createString("reservationUrl");

    public final ListPath<com.example.shimpyo.domain.user.entity.Review, com.example.shimpyo.domain.user.entity.QReview> reviews = this.<com.example.shimpyo.domain.user.entity.Review, com.example.shimpyo.domain.user.entity.QReview>createList("reviews", com.example.shimpyo.domain.user.entity.Review.class, com.example.shimpyo.domain.user.entity.QReview.class, PathInits.DIRECT2);

    public final ListPath<com.example.shimpyo.domain.survey.entity.SuggestionTourist, com.example.shimpyo.domain.survey.entity.QSuggestionTourist> suggestionTourists = this.<com.example.shimpyo.domain.survey.entity.SuggestionTourist, com.example.shimpyo.domain.survey.entity.QSuggestionTourist>createList("suggestionTourists", com.example.shimpyo.domain.survey.entity.SuggestionTourist.class, com.example.shimpyo.domain.survey.entity.QSuggestionTourist.class, PathInits.DIRECT2);

    //inherited
    public final StringPath tel = _super.tel;

    public final ListPath<TouristCategory, QTouristCategory> touristCategories = this.<TouristCategory, QTouristCategory>createList("touristCategories", TouristCategory.class, QTouristCategory.class, PathInits.DIRECT2);

    public final ListPath<TouristOffer, QTouristOffer> touristOffers = this.<TouristOffer, QTouristOffer>createList("touristOffers", TouristOffer.class, QTouristOffer.class, PathInits.DIRECT2);

    //inherited
    public final DateTimePath<java.time.LocalDateTime> updatedAt = _super.updatedAt;

    public QTourist(String variable) {
        super(Tourist.class, forVariable(variable));
    }

    public QTourist(Path<? extends Tourist> path) {
        super(path.getType(), path.getMetadata());
    }

    public QTourist(PathMetadata metadata) {
        super(Tourist.class, metadata);
    }

}

