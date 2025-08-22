package com.example.shimpyo.domain.course.service;

import com.example.shimpyo.domain.auth.service.AuthService;
import com.example.shimpyo.domain.course.repository.LikesRepository;
import com.example.shimpyo.domain.tourist.entity.Category;
import com.example.shimpyo.domain.tourist.entity.Tourist;
import com.example.shimpyo.domain.tourist.service.TouristService;
import com.example.shimpyo.domain.user.dto.TouristLikesResponseDto;
import com.example.shimpyo.domain.user.entity.Likes;
import com.example.shimpyo.domain.user.entity.User;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@Transactional
@RequiredArgsConstructor
public class LikesService {
    private final LikesRepository likesRepository;
    private final AuthService authService;
    private final TouristService touristService;

    public void toggleLikeTourist(Long id) {
        User user = authService.findUser().getUser();
        Tourist tourist = touristService.findTourist(id);
        Optional<Likes> likes = likesRepository.findByUserAndTourist(user, tourist);
        if (likes.isPresent()) {
            likesRepository.delete(likes.get());
            user.getLikes().remove(likes.get());
        } else {
            user.getLikes().add(likesRepository.save(Likes.builder().user(user).tourist(tourist).build()));
        }
    }

    public List<TouristLikesResponseDto> getTouristLikes(String category, Long id) {
        User user = authService.findUser().getUser();
        Pageable pageable = PageRequest.of(0, 8);

        List<Likes> foundLikes = "all".equalsIgnoreCase(category) ? likesRepository.findLikesDtoByUser(user, id, pageable) :
                likesRepository.findLikesDtoByUserAndCategory(user, id, Category.fromCode(category), pageable);
        return foundLikes.stream().map(TouristLikesResponseDto::toDto).collect(Collectors.toList());
    }
}
