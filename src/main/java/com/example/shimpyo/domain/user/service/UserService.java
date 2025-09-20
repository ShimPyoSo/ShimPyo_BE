package com.example.shimpyo.domain.user.service;

import com.example.shimpyo.domain.auth.service.AuthService;
import com.example.shimpyo.domain.tourist.service.TouristService;
import com.example.shimpyo.domain.user.dto.*;
import com.example.shimpyo.domain.user.entity.User;
import com.example.shimpyo.domain.user.repository.UserRepository;
import com.example.shimpyo.global.BaseException;
import com.example.shimpyo.global.exceptionType.MemberException;
import com.example.shimpyo.domain.utils.SecurityUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

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
                .orElseThrow(() -> new BaseException(MemberException.MEMBER_NOT_FOUND))
                .changeNickname(nickname);
    }

    public void checkNickname(String nickname) {
        if (userRepository.existsByNickname(nickname))
            throw new BaseException(MemberException.NICKNAME_DUPLICATED);
    }

    public List<SeenTouristResponseDto> getLastSeenTourists(List<Long> touristIds) {

        return touristIds.stream().map(t -> SeenTouristResponseDto.toDto(touristService.findTourist(t)))
                .collect(Collectors.toList());
    }

    public MyReviewDetailResponseDto getMyReviewTourists(Long touristId) {

        User user = authService.findUser().getUser();
        return MyReviewDetailResponseDto.toDto(touristService.findTourist(touristId),
               touristService.getReviewByUserAndTouristId(touristId, user).stream().map(ReviewDetailDto::toDto).collect(Collectors.toList()));
    }
}
