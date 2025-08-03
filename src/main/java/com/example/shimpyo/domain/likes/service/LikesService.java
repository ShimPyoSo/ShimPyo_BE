package com.example.shimpyo.domain.likes.service;

import com.example.shimpyo.domain.auth.service.AuthService;
import com.example.shimpyo.domain.course.dto.UserCourseResponseDto;
import com.example.shimpyo.domain.course.entity.UserCourse;
import com.example.shimpyo.domain.course.entity.UserCourseDetail;
import com.example.shimpyo.domain.course.entity.UserCourseTourist;
import com.example.shimpyo.domain.course.repository.UserCourseRepository;
import com.example.shimpyo.domain.likes.controller.repository.LikesRepository;
import com.example.shimpyo.domain.tourist.entity.Category;
import com.example.shimpyo.domain.tourist.entity.Tourist;
import com.example.shimpyo.domain.tourist.service.TouristService;
import com.example.shimpyo.domain.user.dto.TouristLikesResponseDto;
import com.example.shimpyo.domain.user.entity.Likes;
import com.example.shimpyo.domain.user.entity.User;
import com.example.shimpyo.global.BaseException;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static com.example.shimpyo.global.exceptionType.LikesException.NOT_FOUND_OR_NOT_OWNED;

@Service
@Transactional
@RequiredArgsConstructor
public class LikesService {
    private final LikesRepository likesRepository;
    private final AuthService authService;
    private final TouristService touristService;
    private final UserCourseRepository userCourseRepository;

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

        return "all".equalsIgnoreCase(category)? likesRepository.findLikesDtoByUser(user, id, pageable):
                likesRepository.findLikesDtoByUserAndCategory(user, id, Category.fromCode(category), pageable);
    }

    public List<?> getCourseLikes(Long id) {
        User user = authService.findUser().getUser();

        List<UserCourse> userCourses = userCourseRepository.findByUser(user);

        UserCourse userCourse = userCourses.stream()
                .filter(course -> course.getId().equals(id))
                .findFirst()
                .orElseThrow(() -> new BaseException(NOT_FOUND_OR_NOT_OWNED));

        List<UserCourseDetail> details = userCourse.getUserCourseDetails();
        List<UserCourseResponseDto> dtos = new ArrayList<>();
        for(UserCourseDetail detail : details) {
            dtos.add(UserCourseResponseDto.from(detail));
        }

        return dtos;
    }
}
