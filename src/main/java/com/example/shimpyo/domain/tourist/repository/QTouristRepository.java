package com.example.shimpyo.domain.tourist.repository;

import com.example.shimpyo.domain.auth.service.AuthService;
import com.example.shimpyo.domain.auth.entity.UserAuth;
import com.example.shimpyo.domain.search.dto.SearchResponseDto;
import com.example.shimpyo.domain.user.entity.User;
import com.example.shimpyo.domain.course.repository.LikesRepository;
import com.example.shimpyo.domain.tourist.dto.FilterRequestDto;
import com.example.shimpyo.domain.tourist.dto.FilterTouristByDataResponseDto;
import com.example.shimpyo.domain.tourist.entity.*;
import com.example.shimpyo.domain.user.entity.QLikes;
import com.example.shimpyo.domain.user.entity.QReview;
import com.example.shimpyo.global.BaseException;
import com.example.shimpyo.domain.utils.RegionUtils;
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

    QTourist tourist = QTourist.tourist;
    QTouristCategory touristCategory = QTouristCategory.touristCategory;
    QTouristOffer touristOffer = QTouristOffer.touristOffer;
    QLikes likes = QLikes.likes;
    QReview review = QReview.review;

    public List<SearchResponseDto> searchResult(FilterRequestDto filter, String keyword) {
        List<Tourist> tourists = filteredTourist(filter, null, keyword);
        Set<Long> likedIds = findLikedIds(tourists);
        List<SearchResponseDto> result = new ArrayList<>();
        for (Tourist t : tourists) {
            boolean isLiked = likedIds.contains(t.getId());
            result.add(SearchResponseDto.toDto(t, isLiked));
        }
        return result;
    }


    private List<Tourist> filteredTourist(FilterRequestDto filter, String category, String keyword) {

        BooleanBuilder whereClause = new BooleanBuilder();

        // 1. 카테고리 또는 키워드 검색
        if (category != null) {
            whereClause.and(categoryCondition(category, touristCategory));
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
//        List<OrderSpecifier<?>> orderBy =

        // 4. 쿼리 실행
        return getTouristsBySort(filter.getSortBy(), whereClause);
        /*return queryFactory
                .selectFrom(tourist)
                .leftJoin(tourist.touristCategories, touristCategory)
                .leftJoin(tourist.touristOffers, touristOffer)
                .leftJoin(tourist.likes, likes)
                .leftJoin(tourist.reviews, review)
                .where(whereClause)
                .groupBy(tourist.id)
                .orderBy(orderBy.toArray(new OrderSpecifier[0]))
                .limit(8)
                .fetch();*/
    }
    private Set<Long> findLikedIds(List<Tourist>results) {
        // 5. 좋아요 정보 조회 및 응답 생성
        Long userId = authService.findUserAuth()
                .map(UserAuth::getUser)
                .map(User::getId)
                .orElse(null);

        return findLikedIdsForSlice(userId, results);
    }

    private BooleanExpression categoryCondition(String category, QTouristCategory touristCategory) {
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
                .or(tourist.address.lower().like(like));
    }

    private BooleanExpression regionCondition(String region, QTourist tourist) {
        if (region == null || region.isBlank()) {
            return null;
        }

        List<String> regions = Arrays.stream(region.split("\\|"))
                .map(String::trim)
                .filter(s -> !s.isEmpty())
                .map(RegionUtils::convertToRegion) // 지역 코드를 실제 지역명으로 변환
                .filter(Objects::nonNull) // null 값 제거 (유효하지 않은 지역 코드)
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

        if (visitEnd.isBefore(visitStart) || visitStart.isBefore(LocalTime.of(5, 0))) {
            throw new BaseException(INVALID_VISIT_TIME_FORMAT);
        }

        // LocalTime을 문자열로 변환하여 비교하거나 TIME 함수 사용
        return Expressions.booleanTemplate(
                "TIME({0}) > TIME({1}) OR TIME({2}) < TIME({3})",
                visitEnd, tourist.openTime, visitStart, tourist.closeTime
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
        if (gender == null || gender.equalsIgnoreCase("female|male")) {
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

        // 각 연령대 컬럼
        Map<String, NumberExpression<Double>> ageMap = Map.of(
                "20Early", tourist.age20EarlyRatio.coalesce(0.0),
                "20Mid",   tourist.age20MidRatio.coalesce(0.0),
                "20Late",  tourist.age20LateRatio.coalesce(0.0),
                "30Early", tourist.age30EarlyRatio.coalesce(0.0),
                "30Mid",   tourist.age30MidRatio.coalesce(0.0),
                "30Late",  tourist.age30LateRatio.coalesce(0.0),
                "40",      tourist.age40Ratio.coalesce(0.0),
                "50",      tourist.age50Ratio.coalesce(0.0),
                "60",      tourist.age60PlusRatio.coalesce(0.0)
        );

        BooleanExpression result = null;

        // 여러 그룹 처리
        for (String group : ageGroup.split("\\|")) {
            String g = group.trim();
            NumberExpression<Double> selected = ageMap.get(g);
            if (selected == null) throw new BaseException(UNSUPPORTED_AGE_GROUP);

            // 선택된 그룹의 비율이 epsilon 이상인지 체크
            BooleanExpression condition = selected.goe(epsilon);

            // OR로 연결
            result = (result == null) ? condition : result.or(condition);
        }
        return result;
    }

    private BooleanExpression cursorCondition(Long lastId, QTourist tourist) {
        return lastId != null ? tourist.id.gt(lastId) : null;
    }

    private List<Tourist> getTouristsBySort(String sortBy, BooleanBuilder whereClause) {
        List<OrderSpecifier<?>> orderBy = new ArrayList<>();

        if ("popular".equalsIgnoreCase(sortBy)) {
            NumberExpression<Double> maleRatio = tourist.maleRatio.coalesce(0.0);
            NumberExpression<Double> femaleRatio = tourist.femaleRatio.coalesce(0.0);

            NumberExpression<Double> genderScore = new CaseBuilder()
                    .when(maleRatio.lt(femaleRatio)).then(maleRatio)
                    .otherwise(femaleRatio);

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

            NumberExpression<Double> totalScoreExp = genderScore.add(ageScore);
            orderBy.add(totalScoreExp.desc());

        } else if ("likes".equalsIgnoreCase(sortBy)) {
            orderBy.add(likes.count().desc());
        } else if ("review".equalsIgnoreCase(sortBy)) {
            orderBy.add(review.count().desc());
        }

        orderBy.add(tourist.id.asc()); // 동점자 처리용

        // 쿼리 실행
        List<Tourist> tourists = queryFactory
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

        // popular 정렬이면 totalScore 계산
        if ("popular".equalsIgnoreCase(sortBy)) {
            tourists.forEach(t -> {
                Double genderVal = t.getMaleRatio() < t.getFemaleRatio() ? t.getMaleRatio() : t.getFemaleRatio();
                Double ageVal = t.getAge20EarlyRatio() + t.getAge20MidRatio() + t.getAge20LateRatio()
                        + t.getAge30EarlyRatio() + t.getAge30MidRatio() + t.getAge30LateRatio()
                        + t.getAge40Ratio() + t.getAge50Ratio() + t.getAge60PlusRatio();
                t.updateTotalScore(genderVal + ageVal);
            });
        }

        return tourists;
    }

    private Set<Long> findLikedIdsForSlice(Long userId, List<Tourist> slice) {
        if (userId == null || slice.isEmpty()) {
            return Collections.emptySet();
        }
        List<Long> ids = slice.stream().map(Tourist::getId).collect(Collectors.toList());
        return likesRepository.findLikedTouristIds(userId, ids);
    }

    public List<FilterTouristByDataResponseDto> makeFilterResponse(FilterRequestDto filter, String category) {
        List<Tourist> tourists = filteredTourist(filter, category, null);
        Set<Long> likedIds = findLikedIds(tourists);

        return tourists.stream()
                .map(t -> {
                    return FilterTouristByDataResponseDto.from(t, likedIds.contains(t.getId()));
                })
                .collect(Collectors.toList());
    }
}
