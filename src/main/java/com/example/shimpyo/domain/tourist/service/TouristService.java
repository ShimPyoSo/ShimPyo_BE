package com.example.shimpyo.domain.tourist.service;

import com.example.shimpyo.domain.auth.entity.UserAuth;
import com.example.shimpyo.domain.auth.service.AuthService;
import com.example.shimpyo.domain.tourist.dto.LikesResponseDto;
import com.example.shimpyo.domain.tourist.dto.RecommendsResponseDto;
import com.example.shimpyo.domain.tourist.entity.Tourist;
import com.example.shimpyo.domain.tourist.repository.TouristRepository;
import com.example.shimpyo.domain.user.entity.User;
import com.example.shimpyo.domain.user.service.UserService;
import com.example.shimpyo.utils.SecurityUtils;
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

    public List<RecommendsResponseDto> getRecommendTourists() {
        String username = SecurityUtils.getLoginId();
        List<RecommendsResponseDto> responseDto = touristRepository.findRandom8Recommends().stream()
                .map(RecommendsResponseDto::toDto).toList();

        if (username != null) {
            UserAuth user = authService.findUser(username);

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

    public List<LikesResponseDto> getLikesTourists(String name) {
        User user = authService.findUser(name).getUser();
        return user.getLikes().stream().map(el ->
                LikesResponseDto.toDto(el.getTourist())).collect(Collectors.toList());
    }
}
