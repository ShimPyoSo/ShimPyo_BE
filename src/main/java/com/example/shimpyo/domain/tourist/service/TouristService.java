package com.example.shimpyo.domain.tourist.service;

import com.example.shimpyo.domain.auth.entity.UserAuth;
import com.example.shimpyo.domain.auth.service.AuthService;
import com.example.shimpyo.domain.course.repository.LikesRepository;
import com.example.shimpyo.domain.search.repository.AcTermRepository;
import com.example.shimpyo.domain.tourist.dto.*;
import com.example.shimpyo.domain.tourist.entity.Category;
import com.example.shimpyo.domain.tourist.entity.Tourist;
import com.example.shimpyo.domain.tourist.repository.TouristRepository;
import com.example.shimpyo.domain.tourist.util.TouristSpecs;
import com.example.shimpyo.domain.user.dto.MyReviewListResponseDto;
import com.example.shimpyo.domain.user.entity.Review;
import com.example.shimpyo.domain.user.entity.User;
import com.example.shimpyo.domain.user.repository.ReviewRepository;
import com.example.shimpyo.global.BaseException;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.stream.Collectors;

import static com.example.shimpyo.global.exceptionType.TouristException.REVIEW_NOT_FOUND;
import static com.example.shimpyo.global.exceptionType.TouristException.TOURIST_NOT_FOUND;

@Service
@Transactional
@RequiredArgsConstructor
public class TouristService {

    private final AuthService authService;
    private final ReviewRepository reviewRepository;
    private final TouristRepository touristRepository;
    private final LikesRepository likesRepository;
    private final AcTermRepository acTermRepository;

    private final Pageable pageable = PageRequest.of(0, 8);

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
                .image(requestDto.getImages())
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

    @Transactional(readOnly = true)
    public List<FilterTouristByDataResponseDto> filteredTouristByCategory(
            String category, FilterRequestDto filter) {

        Specification<Tourist> specification = Specification
                .where(TouristSpecs.byCategory(category))
                .and(TouristSpecs.inRegion(filter.getRegion()))
                .and(TouristSpecs.openWithin(filter.getVisitTime()))
                .and(TouristSpecs.hasAllService(filter.getFacilities()))
                .and(TouristSpecs.genderBias(filter.getGender()))
                .and(TouristSpecs.matchesAgeGroup(filter.getAgeGroup()))
                .and(TouristSpecs.cursorBeforeId(filter.getLastId()));

//        if(filter.getSortBy() == null || "찜 많은순".equalsIgnoreCase(filter.getSortBy())){
//            specification = specification.and(TouristSpecs.orderByLikesCount(Sort.Direction.DESC));
//        }else if("후기순".equalsIgnoreCase(filter.getSortBy())){
//            specification = specification.and(TouristSpecs.orderByReviewCount(Sort.Direction.DESC));
//        }

        //pageable 에서 정렬 빼기
        List<Tourist> rows = touristRepository.findAll(specification, pageable).getContent();

        Long userId = authService.findUserAuth()
                .map(UserAuth::getUser).map(User::getId).orElse(null);


        Set<Long> likedIds = findLikedIdsForSlice(userId, rows);

        return toResponse(rows, likedIds);
    }

    @Transactional(readOnly = true)
    public List<FilterTouristByDataResponseDto> filteredTouristBySearch(
            String keyword, FilterRequestDto filter) {

        Specification<Tourist> specification = Specification
                .where(TouristSpecs.containsSearch(keyword))
                .and(TouristSpecs.inRegion(filter.getRegion()))
                .and(TouristSpecs.openWithin(filter.getVisitTime()))
                .and(TouristSpecs.hasAllService(filter.getFacilities()))
                .and(TouristSpecs.genderBias(filter.getGender()))
                .and(TouristSpecs.matchesAgeGroup(filter.getAgeGroup()))
                .and(TouristSpecs.cursorBeforeId(filter.getLastId()));

//        if("찜 많은순".equalsIgnoreCase(filter.getSortBy())){
//            specification = specification.and(TouristSpecs.orderByLikesCount(Sort.Direction.DESC));
//        }else if("후기순".equalsIgnoreCase(filter.getSortBy())){
//            specification = specification.and(TouristSpecs.orderByReviewCount(Sort.Direction.DESC));
//        }

        Pageable pageable = PageRequest.of(0, 8);
        List<Tourist> rows = touristRepository.findAll(specification, pageable).getContent();


        Long userId = authService.findUserAuth()
                .map(UserAuth::getUser).map(User::getId).orElse(null);


        Set<Long> likedIds = findLikedIdsForSlice(userId, rows);

        return toResponse(rows, likedIds);
    }
    // 2) 공통 좋아요 배치 조회
    private Set<Long> findLikedIdsForSlice(Long userId, List<Tourist> slice) {
        if (userId == null || slice.isEmpty()) return Collections.emptySet();
        List<Long> ids = slice.stream().map(Tourist::getId).toList();
        return likesRepository.findLikedTouristIds(userId, ids);
    }
    // 3) 최종 매핑
    private List<FilterTouristByDataResponseDto> toResponse(
            List<Tourist> slice, Set<Long> likedIds) {
        List<FilterTouristByDataResponseDto> res = new ArrayList<>(slice.size());
        for (Tourist t : slice) {
            boolean isLiked = likedIds.contains(t.getId());
            Long likesCount = likesRepository.countLikesByTouristId(t.getId());
            Long reviewsCount = reviewRepository.countByTouristId(t.getId());
            res.add(FilterTouristByDataResponseDto.from(t, isLiked, likesCount, reviewsCount));
        }
        return res;
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

    public void deleteOneReview(Long touristId, Long reviewId) {
        User user = authService.findUser().getUser();
        reviewRepository.delete(reviewRepository.findByUserAndTouristIdAndId(user, touristId, reviewId)
                .orElseThrow(() -> new BaseException(REVIEW_NOT_FOUND)));
}

    public void deleteReview(Long touristId) {
        User user = authService.findUser().getUser();
        reviewRepository.deleteAll(reviewRepository.findByUserAndTouristId(user, touristId));
    }

    public List<Tourist> getTouristsByRegionAndCategoryAndCount(List<String> regions, List<String> categories, int count) {
        return touristRepository.findByRegionsAndCategoriesAndOpenTimeIsNotNull(regions, categories, count);
    }
}
