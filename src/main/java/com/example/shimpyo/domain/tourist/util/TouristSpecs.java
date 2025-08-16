package com.example.shimpyo.domain.tourist.util;

import com.example.shimpyo.domain.tourist.entity.Category;
import com.example.shimpyo.domain.tourist.entity.Tourist;
import com.example.shimpyo.global.BaseException;
import jakarta.persistence.criteria.*;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;

import java.time.LocalTime;
import java.util.Arrays;

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
                cb.like(cb.lower(root.get("description")), like)
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

    // 3) 예약 여부
    public static Specification<Tourist> reservationRequired(boolean required){
        if(!required) return null; // true 일 때만 조건
        return (root, query, cb) -> cb.isNotNull(root.get("reservationUrl"));
    }

    // 4) 방문 시간 포함
    public static Specification<Tourist> openWithin(String visitTime){
        if(visitTime == null || !visitTime.contains("-")) return null;

        if (!visitTime.matches("\\s*\\d{2}:\\d{2}\\s*-\\s*\\d{2}:\\d{2}\\s*")) {
            throw new BaseException(INVALID_VISIT_TIME_FORMAT);
        }

        String[] time = visitTime.split("\\s*-\\s*");
        if(time.length != 2) {
            throw new BaseException(INVALID_VISIT_TIME_FORMAT);
        }
        String openTime = time[0].trim();
        String closeTime = time[1].trim();

        // 비정상인 시간 선은 스킵
        LocalTime open =  LocalTime.parse(openTime);
        LocalTime close = LocalTime.parse(closeTime);
        if(close.isBefore(open)) {
            throw new BaseException(INVALID_VISIT_TIME_FORMAT);
        }

        return (root, query, cb) -> cb.and(
                cb.lessThanOrEqualTo(root.get("openTime").as(String.class), openTime),
                cb.greaterThanOrEqualTo(root.get("closeTime").as(String.class), closeTime)
        );
    }

    // 5) 제공 서비스: 파이프 구분 문자열
    public static Specification<Tourist> hasAllService(String requiredServices){
        if(requiredServices == null || requiredServices.isBlank()) return null;

        String[] need = requiredServices.split("\\|");
        return (root, query, cb) -> {
            var expr = cb.conjunction();
            var col = root.get("requiredService").as(String.class); // 문자열 컬럼
            for (String raw : need) {
                String token = raw.trim();
                if (token.isEmpty()) continue;
                // 경계 모호성 줄이기 위해 파이프 포함 패턴 사용
                var like1 = cb.like(col, "%|" + token + "|%");
                var like2 = cb.like(col, token + "|%");
                var like3 = cb.like(col, "%|" + token);
                var like4 = cb.like(col, token); // 단일 항목만 있는 경우
                expr = cb.and(expr, cb.or(like1, like2, like3, like4));
            }
            return expr;
        };
    }

    // 6) 성별 가중: male/female
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

    // 7) 연령대
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

            Expression<Double> selected;

            switch (ageGroup){
                case "20대 초반": selected = a20e; break;
                case "20대 중반": selected = a20m; break;
                case "20대 후반": selected = a20l; break;
                case "30대 초반": selected = a30e; break;
                case "30대 중반": selected = a30m; break;
                case "30대 후반": selected = a30l; break;
                case "40대":      selected = a40;  break;
                case "50대":      selected = a50;  break;
                case "60대 이상":  selected = a60p; break;
                default: throw new BaseException(UNSUPPORTED_AGE_GROUP);
            }

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

//    public static Specification<Tourist> orderByLikesCount(Sort.Direction dir) {
//        return (root, query, cb) -> {
//            // count 쿼리일 때는 정렬/그룹 적용하지 않음
//            if (!Long.class.equals(query.getResultType())){
//                var likes = root.join("likes", JoinType.LEFT);
//                var likesCount = cb.countDistinct(likes);
//                query.groupBy(root.get("id"));
//                // 동률 이라면
//                if(dir.isAscending()){
//                    query.orderBy(cb.asc(likesCount), cb.asc(root.get("id")));
//                } else{
//                    query.orderBy(cb.desc(likesCount), cb.asc(root.get("id")));
//                }
//                query.distinct(true);
//            }
//            return cb.conjunction();
//        };
//    }
//
//    public static Specification<Tourist> orderByReviewCount(Sort.Direction dir) {
//        return (root, query, cb) -> {
//            if (!Long.class.equals(query.getResultType())){
//                var review = root.join("review", JoinType.LEFT);
//                var reviewsCount = cb.countDistinct(review);
//                query.groupBy(root.get("id"));
//                if(dir.isAscending()){
//                    query.orderBy(cb.asc(reviewsCount), cb.asc(root.get("id")));
//                }else{
//                    query.orderBy(cb.desc(reviewsCount), cb.asc(root.get("id")));
//                }
//            }
//            return cb.conjunction();
//        };
//    }
}
