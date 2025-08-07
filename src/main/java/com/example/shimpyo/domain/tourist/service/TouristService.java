package com.example.shimpyo.domain.tourist.service;

import com.example.shimpyo.domain.auth.entity.UserAuth;
import com.example.shimpyo.domain.auth.service.AuthService;
import com.example.shimpyo.domain.tourist.dto.*;
import com.example.shimpyo.domain.tourist.entity.Tourist;
import com.example.shimpyo.domain.tourist.repository.TouristRepository;
import com.example.shimpyo.domain.user.dto.MyReviewDetailResponseDto;
import com.example.shimpyo.domain.user.dto.MyReviewListResponseDto;
import com.example.shimpyo.domain.user.dto.ReviewDetailDto;
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

    public List<MyReviewListResponseDto> getMyReviewLists() {
        return reviewRepository.countReviewsByTouristForUser(authService.findUser().getUser().getId());
    }
}
