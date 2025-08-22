package com.example.shimpyo.domain.user.entity;

import static com.querydsl.core.types.PathMetadataFactory.*;

import com.querydsl.core.types.dsl.*;

import com.querydsl.core.types.PathMetadata;
import javax.annotation.processing.Generated;
import com.querydsl.core.types.Path;
import com.querydsl.core.types.dsl.PathInits;


/**
 * QUser is a Querydsl query type for User
 */
@Generated("com.querydsl.codegen.DefaultEntitySerializer")
public class QUser extends EntityPathBase<User> {

    private static final long serialVersionUID = 265532715L;

    private static final PathInits INITS = PathInits.DIRECT2;

    public static final QUser user = new QUser("user");

    public final com.example.shimpyo.domain.common.QBaseEntity _super = new com.example.shimpyo.domain.common.QBaseEntity(this);

    public final NumberPath<Integer> birthYear = createNumber("birthYear", Integer.class);

    //inherited
    public final DateTimePath<java.time.LocalDateTime> createdAt = _super.createdAt;

    //inherited
    public final DateTimePath<java.time.LocalDateTime> deletedAt = _super.deletedAt;

    public final StringPath email = createString("email");

    public final StringPath gender = createString("gender");

    public final NumberPath<Long> id = createNumber("id", Long.class);

    public final ListPath<com.example.shimpyo.domain.survey.entity.SuggestionUser, com.example.shimpyo.domain.survey.entity.QSuggestionUser> likedSuggestion = this.<com.example.shimpyo.domain.survey.entity.SuggestionUser, com.example.shimpyo.domain.survey.entity.QSuggestionUser>createList("likedSuggestion", com.example.shimpyo.domain.survey.entity.SuggestionUser.class, com.example.shimpyo.domain.survey.entity.QSuggestionUser.class, PathInits.DIRECT2);

    public final ListPath<Likes, QLikes> likes = this.<Likes, QLikes>createList("likes", Likes.class, QLikes.class, PathInits.DIRECT2);

    public final StringPath nickname = createString("nickname");

    public final ListPath<Review, QReview> reviews = this.<Review, QReview>createList("reviews", Review.class, QReview.class, PathInits.DIRECT2);

    public final ListPath<com.example.shimpyo.domain.survey.entity.Suggestion, com.example.shimpyo.domain.survey.entity.QSuggestion> suggestions = this.<com.example.shimpyo.domain.survey.entity.Suggestion, com.example.shimpyo.domain.survey.entity.QSuggestion>createList("suggestions", com.example.shimpyo.domain.survey.entity.Suggestion.class, com.example.shimpyo.domain.survey.entity.QSuggestion.class, PathInits.DIRECT2);

    //inherited
    public final DateTimePath<java.time.LocalDateTime> updatedAt = _super.updatedAt;

    public final com.example.shimpyo.domain.auth.entity.QUserAuth userAuth;

    public final ListPath<com.example.shimpyo.domain.course.entity.UserCourse, com.example.shimpyo.domain.course.entity.QUserCourse> userCourses = this.<com.example.shimpyo.domain.course.entity.UserCourse, com.example.shimpyo.domain.course.entity.QUserCourse>createList("userCourses", com.example.shimpyo.domain.course.entity.UserCourse.class, com.example.shimpyo.domain.course.entity.QUserCourse.class, PathInits.DIRECT2);

    public QUser(String variable) {
        this(User.class, forVariable(variable), INITS);
    }

    public QUser(Path<? extends User> path) {
        this(path.getType(), path.getMetadata(), PathInits.getFor(path.getMetadata(), INITS));
    }

    public QUser(PathMetadata metadata) {
        this(metadata, PathInits.getFor(metadata, INITS));
    }

    public QUser(PathMetadata metadata, PathInits inits) {
        this(User.class, metadata, inits);
    }

    public QUser(Class<? extends User> type, PathMetadata metadata, PathInits inits) {
        super(type, metadata, inits);
        this.userAuth = inits.isInitialized("userAuth") ? new com.example.shimpyo.domain.auth.entity.QUserAuth(forProperty("userAuth"), inits.get("userAuth")) : null;
    }

}

