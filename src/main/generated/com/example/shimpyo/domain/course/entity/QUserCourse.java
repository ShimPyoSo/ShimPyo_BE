package com.example.shimpyo.domain.course.entity;

import static com.querydsl.core.types.PathMetadataFactory.*;

import com.querydsl.core.types.dsl.*;

import com.querydsl.core.types.PathMetadata;
import javax.annotation.processing.Generated;
import com.querydsl.core.types.Path;
import com.querydsl.core.types.dsl.PathInits;


/**
 * QUserCourse is a Querydsl query type for UserCourse
 */
@Generated("com.querydsl.codegen.DefaultEntitySerializer")
public class QUserCourse extends EntityPathBase<UserCourse> {

    private static final long serialVersionUID = -1236097162L;

    private static final PathInits INITS = PathInits.DIRECT2;

    public static final QUserCourse userCourse = new QUserCourse("userCourse");

    public final com.example.shimpyo.domain.common.QBaseEntity _super = new com.example.shimpyo.domain.common.QBaseEntity(this);

    //inherited
    public final DateTimePath<java.time.LocalDateTime> createdAt = _super.createdAt;

    //inherited
    public final DateTimePath<java.time.LocalDateTime> deletedAt = _super.deletedAt;

    public final StringPath description = createString("description");

    public final NumberPath<Long> id = createNumber("id", Long.class);

    public final StringPath title = createString("title");

    //inherited
    public final DateTimePath<java.time.LocalDateTime> updatedAt = _super.updatedAt;

    public final com.example.shimpyo.domain.user.entity.QUser user;

    public final ListPath<UserCourseList, QUserCourseList> userCourseLists = this.<UserCourseList, QUserCourseList>createList("userCourseLists", UserCourseList.class, QUserCourseList.class, PathInits.DIRECT2);

    public QUserCourse(String variable) {
        this(UserCourse.class, forVariable(variable), INITS);
    }

    public QUserCourse(Path<? extends UserCourse> path) {
        this(path.getType(), path.getMetadata(), PathInits.getFor(path.getMetadata(), INITS));
    }

    public QUserCourse(PathMetadata metadata) {
        this(metadata, PathInits.getFor(metadata, INITS));
    }

    public QUserCourse(PathMetadata metadata, PathInits inits) {
        this(UserCourse.class, metadata, inits);
    }

    public QUserCourse(Class<? extends UserCourse> type, PathMetadata metadata, PathInits inits) {
        super(type, metadata, inits);
        this.user = inits.isInitialized("user") ? new com.example.shimpyo.domain.user.entity.QUser(forProperty("user"), inits.get("user")) : null;
    }

}

