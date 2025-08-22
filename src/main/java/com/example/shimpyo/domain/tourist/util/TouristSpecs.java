package com.example.shimpyo.domain.tourist.util;

import com.example.shimpyo.domain.tourist.entity.Category;
import com.example.shimpyo.domain.tourist.entity.Offer;
import com.example.shimpyo.domain.tourist.entity.Tourist;
import com.example.shimpyo.global.BaseException;
import jakarta.persistence.criteria.*;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;

import java.time.LocalTime;
import java.util.Arrays;
import java.util.Set;
import java.util.stream.Collectors;

import static com.example.shimpyo.global.exceptionType.TokenException.*;

public final class TouristSpecs {

    private TouristSpecs(){}

    public static Specification<Tourist> cursorBeforeId(Long lastId){
        return (root, query, cb) ->
                lastId == null ? cb.conjunction() : cb.greaterThan(root.get("id"), lastId);
    }

    public static Specification<Tourist> containsSearch(String keyword){
        // 키워드가 없으면 전체 검색
        if(keyword == null || keyword.isBlank()) return null;
        String like = "%" + keyword.toLowerCase().trim() + "%";
        return (root, query, cb) -> cb.or(
                cb.like(cb.lower(root.get("name")), like),
                cb.like(cb.lower(root.get("description")), like),
                cb.like(cb.lower(root.get("address")), like)
        );
    }

    public static Specification<Tourist> byCategory(String category){
        if(category == null || category.equalsIgnoreCase("all")) return null;
        Category cate = Category.fromCode(category);
        return (root, query, cb) -> {
            query.distinct(true);
            var join = root.join("touristCategories", JoinType.INNER);
            return cb.equal(join.get("category"), cate);
        };
    }

    // 2) 지역
    public static Specification<Tourist> inRegion(String region){
        if(region == null || region.isBlank()) return null;

        var regions = Arrays.stream(region.split("\\|"))
                .map(String::trim)
                .filter(s -> !s.isEmpty())
                .distinct()
                .toList();

        if (regions.isEmpty()) return null;

        return (root, query, cb) -> {
            var col = root.get("region").as(String.class);
            var in = cb.in(col);
            for (var r : regions) in.value(r);
            return in;
        };
    }

    // 3) 방문 시간 포함
    public static Specification<Tourist> openWithin(String visitTime) {
        if (visitTime == null || !visitTime.contains("-")) return null;

        if (!visitTime.matches("\\s*\\d{2}:\\d{2}\\s*-\\s*\\d{2}:\\d{2}\\s*")) {
            throw new BaseException(INVALID_VISIT_TIME_FORMAT);
        }

        String[] time = visitTime.split("\\s*-\\s*");
        LocalTime visitStart = LocalTime.parse(time[0].trim());
        // 24:00 처리
        LocalTime visitEnd;
        if (time[1].trim().equals("24:00")) {
            visitEnd = LocalTime.MAX; // 23:59:59.999999999
        } else {
            visitEnd = LocalTime.parse(time[1].trim());
        }

        if (visitEnd.isBefore(visitStart) || visitStart.isBefore(LocalTime.of(9, 0))) {
            throw new BaseException(INVALID_VISIT_TIME_FORMAT);
        }

        return (root, query, cb) -> cb.and(
                cb.lessThanOrEqualTo(root.get("openTime").as(LocalTime.class), visitStart),
                cb.greaterThanOrEqualTo(root.get("closeTime").as(LocalTime.class), visitEnd)
        );
    }

    // 4) 제공 서비스: 파이프 구분 문자열
    public static Specification<Tourist> hasAllService(String requiredServices) {
        if (requiredServices == null || requiredServices.isBlank()) return null;

        Set<Offer> offers = Arrays.stream(requiredServices.split("\\|"))
                .map(String::trim)
                .filter(s -> !s.isEmpty())
                .map(Offer::fromString)
                .collect(Collectors.toSet());

        return (root, query, cb) -> {
            if (offers.isEmpty()) return cb.conjunction();

            var join = root.join("touristOffers", JoinType.INNER);

            var expr = cb.conjunction();
            for (Offer offer : offers) {
                expr = cb.and(expr, cb.equal(join.get("offer"), offer));
            }

            query.distinct(true); // 중복 제거
            return expr;
        };
    }


    // 5) 성별 가중: male/female
    public static Specification<Tourist> genderBias(String gender){
        if (gender == null || gender.equalsIgnoreCase("ALL")) return null;
        return (root, query, cb) -> {
            if (gender.equalsIgnoreCase("male")){
                return cb.greaterThanOrEqualTo(root.get("maleRatio"), root.get("femaleRatio"));
            }else if (gender.equalsIgnoreCase("female")){
                return cb.greaterThanOrEqualTo(root.get("femaleRatio"), root.get("maleRatio"));
            }

            throw new BaseException(UNSUPPORTED_GENDER);
        };
    }

    // 6) 연령대
    public static Specification<Tourist> matchesAgeGroup(String ageGroup){
        return matchesAgeGroup(ageGroup, 1e-6);
    }

    public static Specification<Tourist> matchesAgeGroup(String ageGroup, double epsilon){
        if(ageGroup == null || ageGroup.isBlank() || "ALL".equalsIgnoreCase(ageGroup)) return null;

        return (root, query, cb) -> {
            Expression<Double> a20e = nz(root, "age20EarlyRatio", cb);
            Expression<Double> a20m = nz(root, "age20MidRatio", cb);
            Expression<Double> a20l = nz(root, "age20LateRatio", cb);
            Expression<Double> a30e = nz(root, "age30EarlyRatio", cb);
            Expression<Double> a30m = nz(root, "age30MidRatio", cb);
            Expression<Double> a30l = nz(root, "age30LateRatio", cb);
            Expression<Double> a40  = nz(root, "age40Ratio", cb);
            Expression<Double> a50  = nz(root, "age50Ratio", cb);
            Expression<Double> a60p = nz(root, "age60PlusRatio", cb);

            Expression<Double> selected = switch (ageGroup) {
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

            var eps = cb.literal(epsilon);

            return cb.and(
                cb.ge(selected, cb.diff(a20e, eps)),
                cb.ge(selected, cb.diff(a20m, eps)),
                cb.ge(selected, cb.diff(a20l, eps)),
                cb.ge(selected, cb.diff(a30e, eps)),
                cb.ge(selected, cb.diff(a30m, eps)),
                cb.ge(selected, cb.diff(a30l, eps)),
                cb.ge(selected, cb.diff(a40,  eps)),
                cb.ge(selected, cb.diff(a50,  eps)),
                cb.ge(selected, cb.diff(a60p, eps))
            );
        };
    }

    // null-safe Double 표현식
    private static Expression<Double> nz(Root<Tourist> root, String field, CriteriaBuilder cb){
        var col = root.get(field).as(Double.class);

        return cb.function("coalesce", Double.class, col, cb.literal(0.0d));
    }

    public static Specification<Tourist> orderByLikesCount(Sort.Direction dir) {
        return (root, query, cb) -> {
            // count 쿼리일 때는 정렬/그룹 적용하지 않음
            if (!Long.class.equals(query.getResultType())){
                var likes = root.join("likes", JoinType.LEFT);
                var likesCount = cb.countDistinct(likes);
                query.groupBy(root.get("id"));
                // 동률 이라면
                if(dir.isAscending()){
                    query.orderBy(cb.asc(likesCount), cb.asc(root.get("id")));
                } else{
                    query.orderBy(cb.desc(likesCount), cb.asc(root.get("id")));
                }
                query.distinct(true);
            }
            return cb.conjunction();
        };
    }

    public static Specification<Tourist> orderByReviewCount(Sort.Direction dir) {
        return (root, query, cb) -> {
            if (!Long.class.equals(query.getResultType())){
                var review = root.join("reviews", JoinType.LEFT);
                var reviewsCount = cb.count(review);
                query.groupBy(root.get("id"));
                if(dir.isAscending()){
                    query.orderBy(cb.asc(reviewsCount), cb.asc(root.get("id")));
                }else{
                    query.orderBy(cb.desc(reviewsCount), cb.asc(root.get("id")));
                }
            }
            return cb.conjunction();
        };
    }

    public static Specification<Tourist> orderByPopularity(Sort.Direction dir) {
        return (root, query, cb) -> {
            // Count 쿼리가 아닌 경우만 정렬 적용
            if (!Long.class.equals(query.getResultType())) {
                // 성별 비율 계산 (고르게 높을수록 점수 상승)
                var maleRatio = cb.coalesce(root.get("maleRatio"), 0.0);
                var femaleRatio = cb.coalesce(root.get("femaleRatio"), 0.0);
                Expression<Double> genderScore = cb.selectCase()
                        .when(cb.lessThan(maleRatio, femaleRatio), maleRatio)
                        .otherwise(femaleRatio).as(Double.class);;
                // 연령별 비율 합산
                var ageScore = cb.sum(
                        cb.sum(cb.sum(cb.sum(cb.sum(
                                                cb.coalesce(root.get("age20EarlyRatio"), 0.0),
                                                cb.coalesce(root.get("age20MidRatio"), 0.0)
                                        ), cb.coalesce(root.get("age20LateRatio"), 0.0)),
                                        cb.sum(cb.sum(cb.coalesce(root.get("age30EarlyRatio"), 0.0),
                                                        cb.coalesce(root.get("age30MidRatio"), 0.0)),
                                                cb.coalesce(root.get("age30LateRatio"), 0.0))),
                                cb.sum(cb.sum(cb.coalesce(root.get("age40Ratio"), 0.0),
                                                cb.coalesce(root.get("age50Ratio"), 0.0)),
                                        cb.coalesce(root.get("age60PlusRatio"), 0.0))
                        ));

                // 최종 score: 성별 점수와 연령 점수를 합산
                var totalScore = cb.sum(genderScore, ageScore);

                if (dir.isAscending()) {
                    query.orderBy(cb.asc(totalScore), cb.asc(root.get("id")));
                } else {
                    query.orderBy(cb.desc(totalScore), cb.asc(root.get("id")));
                }
            }
            return cb.conjunction();
        };
    }
}
