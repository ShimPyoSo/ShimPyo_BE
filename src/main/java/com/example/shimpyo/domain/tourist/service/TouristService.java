package com.example.shimpyo.domain.tourist.service;

import com.example.shimpyo.domain.auth.entity.UserAuth;
import com.example.shimpyo.domain.auth.service.AuthService;
import com.example.shimpyo.domain.tourist.dto.*;
import com.example.shimpyo.domain.tourist.entity.Category;
import com.example.shimpyo.domain.tourist.entity.Tourist;
import com.example.shimpyo.domain.tourist.entity.TouristCategory;
import com.example.shimpyo.domain.tourist.repository.TouristCategoryRepository;
import com.example.shimpyo.domain.tourist.repository.TouristRepository;
import com.example.shimpyo.domain.user.entity.Review;
import com.example.shimpyo.domain.user.entity.User;
import com.example.shimpyo.domain.user.repository.ReviewRepository;
import com.example.shimpyo.global.BaseException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalTime;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
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

    // 카테고리와 filter 를 동시에 수행
    public List<FilterTouristByCategoryResponseDto> filteredTouristByCategory(String category, FilterRequestDto dto){
        List<TouristCategory> touristCategories = touristCategoryRepository.findByCategory(Category.fromCode(category));

        List<Tourist> filteredTourists= touristCategories.stream()
                .map(TouristCategory::getTourist)
                .filter(tourist -> applyFilters(tourist, dto))
                .toList();

        List<FilterTouristByCategoryResponseDto> responseDtos = new ArrayList<>();
        for(Tourist tourist : filteredTourists){
            responseDtos.add(FilterTouristByCategoryResponseDto.from(tourist, true, dto.getRegion()));
        }

        return responseDtos;
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
            if (tourist.getOperationTime() == null || !tourist.getOperationTime().contains("~")) return false;

            try {
                String[] time = tourist.getOperationTime().split("~");
                LocalTime now = LocalTime.now();
                LocalTime start = LocalTime.parse(time[0].trim());
                LocalTime end = LocalTime.parse(time[1].trim());

                if (now.isBefore(start) || now.isAfter(end)) return false;
            } catch (Exception e) {
                return false;
            }
        }

        // 3. 운영 시간
        if (filter.getVisitTime() != null) {
            if (tourist.getOperationTime() == null || !tourist.getOperationTime().contains("~")) return false;

            try {
                String[] time = tourist.getOperationTime().split("~");
                LocalTime visit = filter.getVisitTime();
                LocalTime start = LocalTime.parse(time[0].trim());
                LocalTime end = LocalTime.parse(time[1].trim());

                if (visit.isBefore(start) || visit.isAfter(end)) return false;
            } catch (Exception e) {
                return false;
            }
        }

        // 4. 제공 서비스
        if (filter.getRequiredService() != null && !filter.getRequiredService().isEmpty()) {
            if (tourist.getRequiredService() == null ||
                    !new HashSet<>(tourist.getRequiredService()).containsAll(filter.getRequiredService())) {
                return false;
            }
        }

        // 5. 성별
//        if (filter.getGender() != null) {
//
//        }

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
                "경기", "강원", "충북", "충남", "전북", "전남", "경북", "경남", "제주");

        return regions.stream()
                .filter(address::contains)
                .findFirst()
                .orElse("전국");
    }
}
