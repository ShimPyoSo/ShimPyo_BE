package com.example.shimpyo.domain.tourist.service;

import com.example.shimpyo.domain.auth.entity.UserAuth;
import com.example.shimpyo.domain.auth.service.AuthService;
import com.example.shimpyo.domain.tourist.dto.RecommendsResponseDto;
import com.example.shimpyo.domain.tourist.entity.Tourist;
import com.example.shimpyo.domain.tourist.repository.TouristRepository;
import com.example.shimpyo.domain.user.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@Transactional
@RequiredArgsConstructor
public class TouristService {

    private final TouristRepository touristRepository;
    private final AuthService authService;

    public List<RecommendsResponseDto> getRecommendTourists(String username) {
        System.out.println("Now user " + username);

        List<RecommendsResponseDto> responseDto = touristRepository.findRandom8Recommends().stream()
                .map(RecommendsResponseDto::toDto).toList();

        if (username != null) {
            UserAuth user = authService.findUser(username);
            System.out.println("Now user" + user.getUser().getNickname());
            Set<Long> likedTouristIds = user.getUser().getLikes().stream()
                    .map(like -> like.getTourist().getId())
                    .collect(Collectors.toSet());
            for (Long likedTouristId : likedTouristIds) {
                System.out.println("Found Like Ids  " + likedTouristId);
            }
            for (RecommendsResponseDto dto : responseDto) {
                if (likedTouristIds.contains(dto.getId()))
                    dto.isLiked = true;
            }
        }
        return responseDto;
    }
}
