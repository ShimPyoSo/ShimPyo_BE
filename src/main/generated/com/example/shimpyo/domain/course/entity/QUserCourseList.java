package com.example.shimpyo.domain.course.entity;

import static com.querydsl.core.types.PathMetadataFactory.*;

import com.querydsl.core.types.dsl.*;

import com.querydsl.core.types.PathMetadata;
import javax.annotation.processing.Generated;
import com.querydsl.core.types.Path;
import com.querydsl.core.types.dsl.PathInits;


/**
 * QUserCourseList is a Querydsl query type for UserCourseList
 */
@Generated("com.querydsl.codegen.DefaultEntitySerializer")
public class QUserCourseList extends EntityPathBase<UserCourseList> {

    private static final long serialVersionUID = 1967792436L;

    private static final PathInits INITS = PathInits.DIRECT2;

    public static final QUserCourseList userCourseList = new QUserCourseList("userCourseList");

    public final com.example.shimpyo.domain.common.QBaseEntity _super = new com.example.shimpyo.domain.common.QBaseEntity(this);

    //inherited
    public final DateTimePath<java.time.LocalDateTime> createdAt = _super.createdAt;

    //inherited
    public final DateTimePath<java.time.LocalDateTime> deletedAt = _super.deletedAt;

    public final NumberPath<Long> id = createNumber("id", Long.class);

    public final com.example.shimpyo.domain.tourist.entity.QTourist tourist;

    //inherited
    public final DateTimePath<java.time.LocalDateTime> updatedAt = _super.updatedAt;

    public final QUserCourse userCourse;

    public QUserCourseList(String variable) {
        this(UserCourseList.class, forVariable(variable), INITS);
    }

    public QUserCourseList(Path<? extends UserCourseList> path) {
        this(path.getType(), path.getMetadata(), PathInits.getFor(path.getMetadata(), INITS));
    }

    public QUserCourseList(PathMetadata metadata) {
        this(metadata, PathInits.getFor(metadata, INITS));
    }

    public QUserCourseList(PathMetadata metadata, PathInits inits) {
        this(UserCourseList.class, metadata, inits);
    }

    public QUserCourseList(Class<? extends UserCourseList> type, PathMetadata metadata, PathInits inits) {
        super(type, metadata, inits);
        this.tourist = inits.isInitialized("tourist") ? new com.example.shimpyo.domain.tourist.entity.QTourist(forProperty("tourist")) : null;
        this.userCourse = inits.isInitialized("userCourse") ? new QUserCourse(forProperty("userCourse"), inits.get("userCourse")) : null;
    }

}

