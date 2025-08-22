package com.example.shimpyo.domain.search.entity;

import static com.querydsl.core.types.PathMetadataFactory.*;

import com.querydsl.core.types.dsl.*;

import com.querydsl.core.types.PathMetadata;
import javax.annotation.processing.Generated;
import com.querydsl.core.types.Path;


/**
 * QAcTerm is a Querydsl query type for AcTerm
 */
@Generated("com.querydsl.codegen.DefaultEntitySerializer")
public class QAcTerm extends EntityPathBase<AcTerm> {

    private static final long serialVersionUID = -1640908245L;

    public static final QAcTerm acTerm = new QAcTerm("acTerm");

    public final StringPath term = createString("term");

    public final StringPath termChoseong = createString("termChoseong");

    public final NumberPath<Long> termId = createNumber("termId", Long.class);

    public final StringPath termNorm = createString("termNorm");

    public QAcTerm(String variable) {
        super(AcTerm.class, forVariable(variable));
    }

    public QAcTerm(Path<? extends AcTerm> path) {
        super(path.getType(), path.getMetadata());
    }

    public QAcTerm(PathMetadata metadata) {
        super(AcTerm.class, metadata);
    }

}

