package com.example.shimpyo.domain.tourist.repository;

import com.example.shimpyo.domain.auth.service.AuthService;
import com.example.shimpyo.domain.auth.entity.UserAuth;
import com.example.shimpyo.domain.user.entity.User;
import com.example.shimpyo.domain.course.repository.LikesRepository;
import com.example.shimpyo.domain.tourist.dto.FilterRequestDto;
import com.example.shimpyo.domain.tourist.dto.FilterTouristByDataResponseDto;
import com.example.shimpyo.domain.tourist.entity.*;
import com.example.shimpyo.domain.user.entity.QLikes;
import com.example.shimpyo.domain.user.entity.QReview;
import com.example.shimpyo.global.BaseException;
import com.querydsl.core.BooleanBuilder;
import com.querydsl.core.types.OrderSpecifier;
import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.core.types.dsl.CaseBuilder;
import com.querydsl.core.types.dsl.Expressions;
import com.querydsl.core.types.dsl.NumberExpression;
import com.querydsl.jpa.JPAExpressions;
import com.querydsl.jpa.impl.JPAQueryFactory;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalTime;
import java.util.*;
import java.util.stream.Collectors;

import static com.example.shimpyo.global.exceptionType.TokenException.*;

@Repository
@RequiredArgsConstructor
public class QTouristRepository {
    private final JPAQueryFactory queryFactory;
    private final LikesRepository likesRepository;
    private final AuthService authService;

    @Transactional(readOnly = true)
    public List<FilterTouristByDataResponseDto> filteredTourist(FilterRequestDto filter, String category, String keyword) {
        QTourist tourist = QTourist.tourist;
        QTouristCategory touristCategory = QTouristCategory.touristCategory;
        QTouristOffer touristOffer = QTouristOffer.touristOffer;
        QLikes likes = QLikes.likes;
        QReview review = QReview.review;

        BooleanBuilder whereClause = new BooleanBuilder();

        // 1. 카테고리 또는 키워드 검색
        if (category != null) {
            whereClause.and(categoryCondition(category, tourist, touristCategory));
        } else if (keyword != null && !keyword.isBlank()) {
            whereClause.and(keywordCondition(keyword, tourist));
        }

        // 2. 나머지 필터 조건들
        whereClause.and(regionCondition(filter.getRegion(), tourist));
        whereClause.and(visitTimeCondition(filter.getVisitTime(), tourist));
        whereClause.and(facilitiesCondition(filter.getFacilities(), tourist, touristOffer));
        whereClause.and(genderCondition(filter.getGender(), tourist));
        whereClause.and(ageGroupCondition(filter.getAgeGroup(), tourist));
        whereClause.and(cursorCondition(filter.getLastId(), tourist));

        // 3. 정렬 조건
        List<OrderSpecifier<?>> orderBy = getOrderSpecifiers(filter.getSortBy(), tourist, likes, review);

        // 4. 쿼리 실행
        List<Tourist> results = queryFactory
                .selectFrom(tourist)
                .leftJoin(tourist.touristCategories, touristCategory)
                .leftJoin(tourist.touristOffers, touristOffer)
                .leftJoin(tourist.likes, likes)
                .leftJoin(tourist.reviews, review)
                .where(whereClause)
                .groupBy(tourist.id)
                .orderBy(orderBy.toArray(new OrderSpecifier[0]))
                .limit(8)
                .fetch();

        // 5. 좋아요 정보 조회 및 응답 생성
        Long userId = authService.findUserAuth()
                .map(UserAuth::getUser)
                .map(User::getId)
                .orElse(null);

        Set<Long> likedIds = findLikedIdsForSlice(userId, results);
        return toResponse(results, likedIds);
    }

    private BooleanExpression categoryCondition(String category, QTourist tourist, QTouristCategory touristCategory) {
        if (category == null || category.equalsIgnoreCase("all")) {
            return null;
        }
        Category cate = Category.fromCode(category);
        return touristCategory.category.eq(cate);
    }

    private BooleanExpression keywordCondition(String keyword, QTourist tourist) {
        if (keyword == null || keyword.isBlank()) {
            return null;
        }
        String like = "%" + keyword.toLowerCase().trim() + "%";
        return tourist.name.lower().like(like)
                .or(tourist.description.lower().like(like))
                .or(tourist.address.lower().like(like));
    }

    private BooleanExpression regionCondition(String region, QTourist tourist) {
        if (region == null || region.isBlank()) {
            return null;
        }

        List<String> regions = Arrays.stream(region.split("\\|"))
                .map(String::trim)
                .filter(s -> !s.isEmpty())
                .distinct()
                .collect(Collectors.toList());

        if (regions.isEmpty()) {
            return null;
        }

        return tourist.region.in(regions);
    }

    private BooleanExpression visitTimeCondition(String visitTime, QTourist tourist) {
        if (visitTime == null || !visitTime.contains("-")) {
            return null;
        }

        if (!visitTime.matches("\\s*\\d{2}:\\d{2}\\s*-\\s*\\d{2}:\\d{2}\\s*")) {
            throw new BaseException(INVALID_VISIT_TIME_FORMAT);
        }

        String[] time = visitTime.split("\\s*-\\s*");
        LocalTime visitStart = LocalTime.parse(time[0].trim());

        LocalTime visitEnd;
        if (time[1].trim().equals("24:00")) {
            visitEnd = LocalTime.MAX;
        } else {
            visitEnd = LocalTime.parse(time[1].trim());
        }

        if (visitEnd.isBefore(visitStart) || visitStart.isBefore(LocalTime.of(9, 0))) {
            throw new BaseException(INVALID_VISIT_TIME_FORMAT);
        }

        // LocalTime을 문자열로 변환하여 비교하거나 TIME 함수 사용
        return Expressions.booleanTemplate(
                "TIME({0}) <= TIME({1}) AND TIME({2}) >= TIME({3})",
                tourist.openTime, visitStart, tourist.closeTime, visitEnd
        );
    }

    private BooleanExpression facilitiesCondition(String requiredServices, QTourist tourist, QTouristOffer touristOffer) {
        if (requiredServices == null || requiredServices.isBlank()) {
            return null;
        }

        Set<Offer> offers = Arrays.stream(requiredServices.split("\\|"))
                .map(String::trim)
                .filter(s -> !s.isEmpty())
                .map(Offer::fromString)
                .collect(Collectors.toSet());

        if (offers.isEmpty()) {
            return null;
        }

        BooleanExpression result = null;
        for (Offer offer : offers) {
            BooleanExpression hasOffer = JPAExpressions
                    .selectOne()
                    .from(touristOffer)
                    .where(touristOffer.tourist.eq(tourist)
                            .and(touristOffer.offer.eq(offer)))
                    .exists();

            result = result == null ? hasOffer : result.and(hasOffer);
        }

        return result;
    }

    private BooleanExpression genderCondition(String gender, QTourist tourist) {
        if (gender == null || gender.equalsIgnoreCase("ALL")) {
            return null;
        }

        if (gender.equalsIgnoreCase("male")) {
            return tourist.maleRatio.goe(tourist.femaleRatio);
        } else if (gender.equalsIgnoreCase("female")) {
            return tourist.femaleRatio.goe(tourist.maleRatio);
        }

        throw new BaseException(UNSUPPORTED_GENDER);
    }

    private BooleanExpression ageGroupCondition(String ageGroup, QTourist tourist) {
        if (ageGroup == null || ageGroup.isBlank() || "ALL".equalsIgnoreCase(ageGroup)) {
            return null;
        }

        double epsilon = 1e-6;

        NumberExpression<Double> a20e = tourist.age20EarlyRatio.coalesce(0.0);
        NumberExpression<Double> a20m = tourist.age20MidRatio.coalesce(0.0);
        NumberExpression<Double> a20l = tourist.age20LateRatio.coalesce(0.0);
        NumberExpression<Double> a30e = tourist.age30EarlyRatio.coalesce(0.0);
        NumberExpression<Double> a30m = tourist.age30MidRatio.coalesce(0.0);
        NumberExpression<Double> a30l = tourist.age30LateRatio.coalesce(0.0);
        NumberExpression<Double> a40 = tourist.age40Ratio.coalesce(0.0);
        NumberExpression<Double> a50 = tourist.age50Ratio.coalesce(0.0);
        NumberExpression<Double> a60p = tourist.age60PlusRatio.coalesce(0.0);

        NumberExpression<Double> selected = switch (ageGroup) {
            case "20Early" -> a20e;
            case "20Mid" -> a20m;
            case "20Late" -> a20l;
            case "30Early" -> a30e;
            case "30Mid" -> a30m;
            case "30Late" -> a30l;
            case "40" -> a40;
            case "50" -> a50;
            case "60" -> a60p;
            default -> throw new BaseException(UNSUPPORTED_AGE_GROUP);
        };

        return selected.goe(a20e.subtract(epsilon))
                .and(selected.goe(a20m.subtract(epsilon)))
                .and(selected.goe(a20l.subtract(epsilon)))
                .and(selected.goe(a30e.subtract(epsilon)))
                .and(selected.goe(a30m.subtract(epsilon)))
                .and(selected.goe(a30l.subtract(epsilon)))
                .and(selected.goe(a40.subtract(epsilon)))
                .and(selected.goe(a50.subtract(epsilon)))
                .and(selected.goe(a60p.subtract(epsilon)));
    }

    private BooleanExpression cursorCondition(Long lastId, QTourist tourist) {
        return lastId != null ? tourist.id.gt(lastId) : null;
    }

    private List<OrderSpecifier<?>> getOrderSpecifiers(String sortBy, QTourist tourist, QLikes likes, QReview review) {
        List<OrderSpecifier<?>> orderBy = new ArrayList<>();

        if ("popular".equalsIgnoreCase(sortBy)) {
            // 성별 비율 점수 계산
            NumberExpression<Double> maleRatio = tourist.maleRatio.coalesce(0.0);
            NumberExpression<Double> femaleRatio = tourist.femaleRatio.coalesce(0.0);

            NumberExpression<Double> genderScore = new CaseBuilder()
                    .when(maleRatio.lt(femaleRatio)).then(maleRatio)
                    .otherwise(femaleRatio);

            // 연령대 점수 합산
            NumberExpression<Double> ageScore =
                    tourist.age20EarlyRatio.coalesce(0.0)
                            .add(tourist.age20MidRatio.coalesce(0.0))
                            .add(tourist.age20LateRatio.coalesce(0.0))
                            .add(tourist.age30EarlyRatio.coalesce(0.0))
                            .add(tourist.age30MidRatio.coalesce(0.0))
                            .add(tourist.age30LateRatio.coalesce(0.0))
                            .add(tourist.age40Ratio.coalesce(0.0))
                            .add(tourist.age50Ratio.coalesce(0.0))
                            .add(tourist.age60PlusRatio.coalesce(0.0));

            NumberExpression<Double> totalScore = genderScore.add(ageScore);
            orderBy.add(totalScore.desc());

        } else if ("likes".equalsIgnoreCase(sortBy)) {
            orderBy.add(likes.count().desc());

        } else if ("review".equalsIgnoreCase(sortBy)) {
            orderBy.add(review.count().desc());
        }

        // 동점자 처리를 위한 ID 정렬
        orderBy.add(tourist.id.asc());
        return orderBy;
    }

    private Set<Long> findLikedIdsForSlice(Long userId, List<Tourist> slice) {
        if (userId == null || slice.isEmpty()) {
            return Collections.emptySet();
        }
        List<Long> ids = slice.stream().map(Tourist::getId).collect(Collectors.toList());
        return likesRepository.findLikedTouristIds(userId, ids);
    }

    private List<FilterTouristByDataResponseDto> toResponse(List<Tourist> slice, Set<Long> likedIds) {
        List<FilterTouristByDataResponseDto> res = new ArrayList<>(slice.size());
        for (Tourist t : slice) {
            boolean isLiked = likedIds.contains(t.getId());
            res.add(FilterTouristByDataResponseDto.from(t, isLiked));
        }
        return res;
    }
}
