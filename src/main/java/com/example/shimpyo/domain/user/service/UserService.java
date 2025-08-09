package com.example.shimpyo.domain.user.service;

import com.example.shimpyo.domain.auth.service.AuthService;
import com.example.shimpyo.domain.tourist.service.TouristService;
import com.example.shimpyo.domain.user.dto.*;
import com.example.shimpyo.domain.user.entity.Review;
import com.example.shimpyo.domain.user.entity.User;
import com.example.shimpyo.domain.user.repository.ReviewRepository;
import com.example.shimpyo.domain.user.repository.UserRepository;
import com.example.shimpyo.global.BaseException;
import com.example.shimpyo.global.exceptionType.MemberExceptionType;
import com.example.shimpyo.utils.SecurityUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;


@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class UserService {

    private final UserRepository userRepository;
    private final TouristService touristService;
    private final AuthService authService;

    @Transactional()
    public void changeNickname(String nickname) {
        userRepository.findById(SecurityUtils.getUserId())
                .orElseThrow(() -> new BaseException(MemberExceptionType.MEMBER_NOT_FOUND))
                .changeNickname(nickname);
    }

    public void checkNickname(String nickname) {
        if (userRepository.existsByNickname(nickname))
            throw new BaseException(MemberExceptionType.NICKNAME_DUPLICATED);
    }

    public List<SeenTouristResponseDto> getLastSeenTourists(List<Long> touristIds) {

        return touristIds.stream().map(t -> SeenTouristResponseDto.toDto(touristService.findTourist(t)))
                .collect(Collectors.toList());
    }

    public MyReviewDetailResponseDto getMyReviewTourists(Long touristId) {

        return MyReviewDetailResponseDto.toDto(touristService.findTourist(touristId),
                authService.findUser().getUser().getReviews()
                        .stream().map(ReviewDetailDto::toDto).collect(Collectors.toList()));

    }
}
