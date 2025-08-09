package com.example.shimpyo.domain.tourist.service;

import com.example.shimpyo.domain.auth.entity.UserAuth;
import com.example.shimpyo.domain.auth.service.AuthService;
import com.example.shimpyo.domain.course.repository.LikesRepository;
import com.example.shimpyo.domain.tourist.dto.*;
import com.example.shimpyo.domain.tourist.entity.Category;
import com.example.shimpyo.domain.tourist.entity.Tourist;
import com.example.shimpyo.domain.tourist.entity.TouristCategory;
import com.example.shimpyo.domain.tourist.repository.TouristCategoryRepository;
import com.example.shimpyo.domain.tourist.repository.TouristRepository;
import com.example.shimpyo.domain.user.dto.MyReviewListResponseDto;
import com.example.shimpyo.domain.user.entity.Review;
import com.example.shimpyo.domain.user.entity.User;
import com.example.shimpyo.domain.user.repository.ReviewRepository;
import com.example.shimpyo.global.BaseException;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static com.example.shimpyo.global.exceptionType.TouristException.TOURIST_NOT_FOUND;

@Service
@Transactional
@RequiredArgsConstructor
public class TouristService {

    private final AuthService authService;
    private final ReviewRepository reviewRepository;
    private final TouristRepository touristRepository;
    private final TouristCategoryRepository touristCategoryRepository;
    private final LikesRepository likesRepository;

    // 오차 방지
    private static final double EPS = 1e-9;

    public List<RecommendsResponseDto> getRecommendTourists() {
        List<RecommendsResponseDto> responseDto = touristRepository.findRandom8Recommends().stream()
                .map(RecommendsResponseDto::toDto).toList();

        if (!(SecurityContextHolder.getContext().getAuthentication() instanceof AnonymousAuthenticationToken)) {
            UserAuth user = authService.findUser();

            Set<Long> likedTouristIds = user.getUser().getLikes().stream()
                    .map(like -> like.getTourist().getId())
                    .collect(Collectors.toSet());

            for (RecommendsResponseDto dto : responseDto) {
                if (likedTouristIds.contains(dto.getId()))
                    dto.isLiked = true;
            }
        }

        return responseDto;
    }

    public List<LikesResponseDto> getLikesTourists() {
        User user = authService.findUser().getUser();
        return user.getLikes().stream().map(el ->
                LikesResponseDto.toDto(el.getTourist())).collect(Collectors.toList());
    }

    public void createReview(ReviewRequestDto requestDto) {
        reviewRepository.save(Review.builder()
                .content(requestDto.getContents())
                .image(List.of(requestDto.getImages().split(", ")))
                .user(authService.findUser().getUser())
                .tourist(touristRepository.findById(requestDto.getId())
                        .orElseThrow(() -> new BaseException(TOURIST_NOT_FOUND)))
                .build());
    }

    public List<ReviewResponseDto> getTouristReview(Long touristId, int limit, Long reviewId) {
        Tourist tourist = touristRepository.findById(touristId)
                .orElseThrow(() -> new BaseException(TOURIST_NOT_FOUND));

        List<Review> result;
        Pageable pageable = PageRequest.of(0, limit);
        if (reviewId == null) {
            result = reviewRepository.findByTouristOrderByIdDesc(tourist, pageable);

        } else {
            result = reviewRepository.findByTouristAndIdLessThanOrderByIdDesc(tourist, reviewId, pageable);
        }
        return result.stream().map(r -> ReviewResponseDto.toDto(r, r.getUser())).collect(Collectors.toList());
    }

    // 공개 메서드: 파라미터만 바꿔 재사용
    @Transactional(readOnly = true)
    public List<FilterTouristByCategoryResponseDto> filteredTouristByCategory(
            String category, FilterRequestDto dto, Pageable pageable) {

        List<Tourist> filtered = filterAndDistinct(touristSource(category), dto);
        List<Tourist> pageSlice = slice(filtered, pageable);

        Long userId = authService.findUserAuth()
                .map(UserAuth::getUser).map(User::getId).orElse(null);

        Set<Long> likedIds = findLikedIdsForSlice(userId, pageSlice);

        return toResponse(pageSlice, likedIds, dto);
    }
    // 0) 소스 스트림: all 이면 전체, 아니면 카테고리에서 Tourist로
    private Stream<Tourist> touristSource(String category) {
        if ("all".equalsIgnoreCase(category)) {
            return touristRepository.findAll().stream();
        }
        return touristCategoryRepository.findByCategory(Category.fromCode(category)).stream()
                .map(TouristCategory::getTourist);
    }
    // 1) 공통 필터 + id 기준 distinct
    private List<Tourist> filterAndDistinct(Stream<Tourist> source, FilterRequestDto dto) {
        return source
                .filter(t -> applyFilters(t, dto))
                .collect(Collectors.collectingAndThen(
                        Collectors.toMap(Tourist::getId, t -> t, (a, b) -> a, LinkedHashMap::new),
                        m -> new ArrayList<>(m.values())
                ));
    }
    // 2) 공통 페이징 슬라이싱
    private List<Tourist> slice(List<Tourist> list, Pageable pageable) {
        int size = list.size();
        int start = (int) pageable.getOffset();
        if (start >= size) return Collections.emptyList();
        int end = Math.min(start + pageable.getPageSize(), size);
        return list.subList(start, end);
    }
    // 3) 공통 좋아요 배치 조회
    private Set<Long> findLikedIdsForSlice(Long userId, List<Tourist> slice) {
        if (userId == null || slice.isEmpty()) return Collections.emptySet();
        List<Long> ids = slice.stream().map(Tourist::getId).toList();
        return likesRepository.findLikedTouristIds(userId, ids);
    }
    // 4) 최종 매핑
    private List<FilterTouristByCategoryResponseDto> toResponse(
            List<Tourist> slice, Set<Long> likedIds, FilterRequestDto dto) {
        List<FilterTouristByCategoryResponseDto> res = new ArrayList<>(slice.size());
        for (Tourist t : slice) {
            boolean isLiked = likedIds.contains(t.getId());
            String region = (dto.getRegion() == null) ? extractRegion(t.getAddress()) : dto.getRegion();
            res.add(FilterTouristByCategoryResponseDto.from(t, isLiked, region));
        }
        return res;
    }
    private boolean applyFilters(Tourist tourist, FilterRequestDto filter) {
        // 1. 지역
            if (filter.getRegion() != null && !filter.getRegion().isBlank()) {
            String region = extractRegion(tourist.getAddress());
            if (!filter.getRegion().equals(region)) return false;
        }

        // 2. 예약 여부
        if (filter.isReservationRequired()) {
            if (tourist.getReservationUrl() == null) return false;
            if (tourist.getOpenTime() == null || tourist.getCloseTime() == null) return false;
        }

        if (filter.getVisitTime() != null && !filter.getVisitTime().isBlank()) {
            if (tourist.getOpenTime() == null || tourist.getCloseTime() == null) return false;

            try {
                // "HH:mm-HH:mm" 형식 파싱 (공백 허용)
                String[] parts = filter.getVisitTime().split("\\s*-\\s*");
                if (parts.length != 2) return false;

                DateTimeFormatter fmt = DateTimeFormatter.ofPattern("HH:mm");
                LocalTime reqStart = LocalTime.parse(parts[0], fmt);
                LocalTime reqEnd   = LocalTime.parse(parts[1], fmt);
                if (reqEnd.isBefore(reqStart)) return false; // 비정상 구간

                LocalTime open  = LocalTime.parse(tourist.getOpenTime(), fmt);
                LocalTime close = LocalTime.parse(tourist.getCloseTime(), fmt); // 마감 1시간 전까지 허용

                // 요청 구간이 영업시간 내에 완전히 포함되는지 (동등 허용)
                if (reqStart.isBefore(open) || reqEnd.isAfter(close)) return false;

            } catch (Exception e) {
                return false; // 포맷 오류 등
            }
        }

        // 4. 제공 서비스
        if (filter.getRequiredService() != null && !filter.getRequiredService().isEmpty()) {
            if (tourist.getRequiredService() == null || tourist.getRequiredService().isBlank()) {
                return false;
            }

            // 관광지 제공 서비스 → Set 변환
            Set<String> have = Arrays.stream(tourist.getRequiredService().split("\\|"))
                    .map(String::trim)
                    .filter(s -> !s.isEmpty())
                    .collect(Collectors.toSet());

            // 필터에서 요구하는 서비스 → Set 변환
            Set<String> need = Arrays.stream(filter.getRequiredService().split("\\|"))
                    .map(String::trim)
                    .filter(s -> !s.isEmpty())
                    .collect(Collectors.toSet());

            // have가 need를 모두 포함하는지 체크
            if (!have.containsAll(need)) {   // ✅ 불일치면 실패로 종료
                return false;
            }
        }

//         5. 성별
        if (filter.getGender() != null && !"ALL".equalsIgnoreCase(filter.getGender())) {
            String g = filter.getGender().toLowerCase();
            if(g.equals("male") && tourist.getMaleRatio() < 0.5) return false;
            if(g.equals("female") && tourist.getFemaleRatio() < 0.5) return false;
        }

        // 6. 연령대
        if (filter.getAgeGroup() != null && !filter.getAgeGroup().isBlank()) {
            if (!matchAgeGroup(tourist, filter.getAgeGroup())) {
                return false;
            }
        }
        return true;
    }



    // 오차 줄이기
    private static boolean nearlyEqual(double a, double b) {
        return Math.abs(a - b) <= EPS;
    }
    // 연령대 매치를 위한 메서드
    private boolean matchAgeGroup(Tourist t, String ageGroup) {
        if (ageGroup == null) return true;

        // 각 연령대 비율 중 최대값 찾기
        double maxRatio = Collections.max(Arrays.asList(
                t.getAge20EarlyRatio(),
                t.getAge20MidRatio(),
                t.getAge20LateRatio(),
                t.getAge30EarlyRatio(),
                t.getAge30MidRatio(),
                t.getAge30LateRatio(),
                t.getAge40Ratio(),
                t.getAge50Ratio(),
                t.getAge60PlusRatio()
        ));

        return switch (ageGroup) {
            case "20대 초반" -> nearlyEqual(t.getAge20EarlyRatio(),  maxRatio);
            case "20대 중반" -> nearlyEqual(t.getAge20MidRatio(),    maxRatio);
            case "20대 후반" -> nearlyEqual(t.getAge20LateRatio(),   maxRatio);
            case "30대 초반" -> nearlyEqual(t.getAge30EarlyRatio(),  maxRatio);
            case "30대 중반" -> nearlyEqual(t.getAge30MidRatio(),    maxRatio);
            case "30대 후반" -> nearlyEqual(t.getAge30LateRatio(),   maxRatio);
            case "40대"      -> nearlyEqual(t.getAge40Ratio(),       maxRatio);
            case "50대"      -> nearlyEqual(t.getAge50Ratio(),       maxRatio);
            case "60대 이상"  -> nearlyEqual(t.getAge60PlusRatio(),   maxRatio);
            default -> true; // 알 수 없는 라벨이면 스킵
        };
    }

    public Tourist findTourist(Long id) {
        return touristRepository.findById(id).orElseThrow(() -> new BaseException(TOURIST_NOT_FOUND));
    }

    public TouristDetailResponseDto getTouristDetail(Long touristId){

        Tourist tourist = touristRepository.findById(touristId)
                .orElseThrow(() -> new BaseException(TOURIST_NOT_FOUND));

        return TouristDetailResponseDto.toDto(tourist, extractRegion(tourist.getAddress()));

    }

    private static String extractRegion(String address) {

        List<String> regions = List.of(
                "서울", "부산", "대구", "인천", "광주", "대전", "울산", "세종",
                "경기", "강원", "충청북도", "충청남도", "전라북도", "전라남도", "경상북도", "경상남도", "제주");
        Map<String, String> shortNames = Map.of(
                "충청북도", "충북",
                "충청남도", "충남",
                "전라북도", "전북",
                "전라남도", "전남",
                "경상북도", "경북",
                "경상남도", "경남"
        );
        return regions.stream()
                .filter(address::contains)
                .map(region -> shortNames.getOrDefault(region, region))
                .findFirst()
                .orElse("전국");
    }

    public List<MyReviewListResponseDto> getMyReviewLists() {
        return reviewRepository.countReviewsByTouristForUser(authService.findUser().getUser().getId());
    }
}
