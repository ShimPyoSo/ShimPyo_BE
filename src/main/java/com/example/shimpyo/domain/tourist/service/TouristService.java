package com.example.shimpyo.domain.tourist.service;

import com.example.shimpyo.domain.auth.entity.UserAuth;
import com.example.shimpyo.domain.auth.service.AuthService;
import com.example.shimpyo.domain.course.repository.LikesRepository;
import com.example.shimpyo.domain.search.dto.SearchResponseDto;
import com.example.shimpyo.domain.tourist.dto.*;
import com.example.shimpyo.domain.tourist.entity.Category;
import com.example.shimpyo.domain.tourist.entity.Tourist;
import com.example.shimpyo.domain.tourist.repository.QTouristRepository;
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
    private final QTouristRepository qTouristRepository;
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
    public List<FilterTouristByDataResponseDto> filteredTourist(FilterRequestDto filter, String category) {
        return qTouristRepository.makeFilterResponse(filter, category);
    }

    @Transactional(readOnly = true)
    public List<SearchResponseDto> searchResults(FilterRequestDto filter, String keyword) {
        return qTouristRepository.searchResult(filter, keyword);
    }

    public Tourist findTourist(Long id) {
        return touristRepository.findById(id).orElseThrow(() -> new BaseException(TOURIST_NOT_FOUND));
    }

    public TouristDetailResponseDto getTouristDetail(Long touristId){
        Tourist tourist = touristRepository.findById(touristId)
                .orElseThrow(() -> new BaseException(TOURIST_NOT_FOUND));
        boolean isLiked = false;
        if (!(SecurityContextHolder.getContext().getAuthentication() instanceof AnonymousAuthenticationToken)) {
            User user = authService.findUser().getUser();
            if (likesRepository.findByUserAndTourist(user, tourist).isPresent())
                isLiked = true;
        }
        return TouristDetailResponseDto.toDto(tourist, isLiked);
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
        reviewRepository.deleteAll(getReviewByUserAndTouristId(touristId, user));
    }

    public List<Review> getReviewByUserAndTouristId(Long touristId, User user) {
        List<Review> result = reviewRepository.findByUserAndTouristId(user, touristId);
        if (result.isEmpty()) {
            throw new BaseException(REVIEW_NOT_FOUND);
        }
        return result;
    }

    public List<Tourist> getTouristsByRegionAndCategoryAndCount(List<String> regions, List<Category> categories, int count) {
        return touristRepository.findByRegionsAndCategoriesAndOpenTimeIsNotNull(regions, categories, count);
    }

    public List<Tourist> findAll() {
        return touristRepository.findAll();
    }

    public List<Tourist> getRecommendsOnAddition(List<Category> categories, List<String> regions) {
        return touristRepository.findByRegionsAndCategories(regions, categories);
    }

    public List<String> findDistinctRegionDetailsByRegion(String region) {
        return touristRepository.findDistinctRegionDetailsByRegion(region);
    }

    /**
     * regionDetail 기준으로 category 필터 후 랜덤으로 count 만큼 반환
     */
    public List<Tourist> getTouristsByRegionDetailAndCategoryAndCount(

            String region, String regionDetail, List<Category> categories, int count) {

        List<Tourist> candidates = touristRepository.findByRegionDetailAndCategories(region, regionDetail, categories);

        if (candidates == null || candidates.isEmpty()) {
            return Collections.emptyList();
        }

        Collections.shuffle(candidates);
        return candidates.stream()
                .limit(count)
                .collect(Collectors.toList());
    }
}
