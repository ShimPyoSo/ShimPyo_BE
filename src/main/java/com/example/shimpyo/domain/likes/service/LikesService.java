package com.example.shimpyo.domain.likes.service;

import com.example.shimpyo.domain.auth.service.AuthService;
import com.example.shimpyo.domain.likes.controller.repository.LikesRepository;
import com.example.shimpyo.domain.tourist.entity.Tourist;
import com.example.shimpyo.domain.tourist.service.TouristService;
import com.example.shimpyo.domain.user.entity.Likes;
import com.example.shimpyo.domain.user.entity.User;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

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
}
