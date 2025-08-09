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
import java.util.*;
import java.util.stream.Collectors;

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

    // 관광지 카테고리 및 조건 별 필터링
    @Transactional(readOnly = true)
    public List<FilterTouristByCategoryResponseDto> filteredTouristByCategory(
            String category, FilterRequestDto dto, Pageable pageable) {

        // 1) 카테고리 → 관광지 필터 + 중복 제거
        List<Tourist> filtered = touristCategoryRepository.findByCategory(Category.fromCode(category)).stream()
                .map(TouristCategory::getTourist)
                .filter(t -> applyFilters(t, dto))
                // id 기준 distinct (순서 보존)
                .collect(Collectors.collectingAndThen(
                        Collectors.toMap(Tourist::getId, t -> t, (a, b) -> a, LinkedHashMap::new),
                        m -> new ArrayList<>(m.values())
                ));

        // 2) 페이징 슬라이싱 먼저
        int size = filtered.size();
        int start = (int) pageable.getOffset();
        if (start >= size) return Collections.emptyList();
        int end = Math.min(start + pageable.getPageSize(), size);
        List<Tourist> pageSlice = filtered.subList(start, end);

        // 3) 로그인 유저 (없어도 통과)
        Long userId = authService.findUserAuth()
                .map(UserAuth::getUser)
                .map(User::getId)
                .orElse(null);

        // 4) 현재 페이지에 한해 좋아요 배치 조회
        Set<Long> likedIds = Collections.emptySet();
        if (userId != null && !pageSlice.isEmpty()) {
            List<Long> pageIds = pageSlice.stream()
                    .map(Tourist::getId)
                    .toList();
            likedIds = likesRepository.findLikedTouristIds(userId, pageIds);
        }

        // 5) DTO 매핑 (for문)
        List<FilterTouristByCategoryResponseDto> response = new ArrayList<>(pageSlice.size());
        for (Tourist t : pageSlice) {
            boolean isLiked = (userId != null) && likedIds.contains(t.getId());
            response.add(FilterTouristByCategoryResponseDto.from(t, isLiked, (dto.getRegion()==null)?extractRegion(t.getAddress()) : dto.getRegion()));
        }
        return response;
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

            try {
                LocalTime now = LocalTime.now();
                LocalTime start = LocalTime.parse(tourist.getOpenTime());
                LocalTime end = LocalTime.parse(tourist.getCloseTime());

                if (now.isBefore(start) || now.isAfter(end)) return false;
            } catch (Exception e) {
                return false;
            }
        }

        // 3. 운영 시간
        if (filter.getVisitTime() != null) {
            if (tourist.getCloseTime() == null || tourist.getOpenTime() == null) return false;

            try {
                LocalTime visit = filter.getVisitTime();
                LocalTime start = LocalTime.parse(tourist.getOpenTime());
                LocalTime end = LocalTime.parse(tourist.getCloseTime()).minusHours(1);

                if (visit.isBefore(start) || visit.isAfter(end)) return false;
            } catch (Exception e) {
                return false;
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
//        if (filter.getAgeGroup() != null) {
//            if (tourist.getTargetAge() == null) {
//                return false;
//            }
//        }

        return true;
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

        return regions.stream()
                .filter(address::contains)
                .findFirst()
                .orElse("전국");
    }

    public List<MyReviewListResponseDto> getMyReviewLists() {
        return reviewRepository.countReviewsByTouristForUser(authService.findUser().getUser().getId());
    }
}
