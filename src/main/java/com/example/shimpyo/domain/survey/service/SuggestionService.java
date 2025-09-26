package com.example.shimpyo.domain.survey.service;

import com.example.shimpyo.domain.auth.service.AuthService;
import com.example.shimpyo.domain.auth.service.RedisService;
import com.example.shimpyo.domain.course.dto.AdditionRecommendsResponseDto;
import com.example.shimpyo.domain.course.dto.ChangeTitleRequestDto;
import com.example.shimpyo.domain.survey.dto.CourseResponseDto;
import com.example.shimpyo.domain.survey.dto.CourseUpdateRequestDto;
import com.example.shimpyo.domain.survey.dto.SuggestionRedisDto;
import com.example.shimpyo.domain.survey.entity.*;
import com.example.shimpyo.domain.tourist.repository.CustomTouristRepository;
import com.example.shimpyo.domain.survey.repository.SuggestionRepository;
import com.example.shimpyo.domain.survey.repository.SuggestionTouristRepository;
import com.example.shimpyo.domain.tourist.entity.CustomTourist;
import com.example.shimpyo.domain.tourist.entity.Tourist;
import com.example.shimpyo.domain.tourist.service.TouristService;
import com.example.shimpyo.domain.user.dto.LikedCourseResponseDto;
import com.example.shimpyo.domain.user.entity.User;
import com.example.shimpyo.domain.utils.RegionUtils;
import com.example.shimpyo.global.BaseException;
import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

import static com.example.shimpyo.global.exceptionType.CourseException.ALREADY_LIKED;
import static com.example.shimpyo.global.exceptionType.CourseException.COURSE_NOT_FOUND;

@Service
@Transactional
@RequiredArgsConstructor
public class SuggestionService {

    private final AuthService authService;
    private final RedisService redisService;
    private final TouristService touristService;
    private final SuggestionTouristRepository stRepository;
    private final SuggestionRepository suggestionRepository;
    private final CustomTouristRepository customTouristRepository;

    @Getter
    @EqualsAndHashCode
    @AllArgsConstructor
    static class SuggestionSortKey {
        private Long touristId;
        private String date;
        private LocalTime time;
    }

    public Long likeCourse(String token) {
        User user = authService.findUser().getUser();
        SuggestionRedisDto dto = redisService.findSuggestionByTempId(token);
        if (!user.getId().equals(dto.getUserId()))
            throw new BaseException(COURSE_NOT_FOUND);
       if (redisService.existsSuggestionByToken(token)) {
            return saveSuggestionFromTemp(user, dto.getCourse());
        }
        throw new BaseException(ALREADY_LIKED);
    }

    public Long saveSuggestionFromTemp(User user, CourseResponseDto dto) {
        // 2. Suggestion 생성
        Suggestion suggestion = Suggestion.builder()
                .title(dto.getTitle())
                .token(dto.getToken())
                .wellnessType(WellnessType.fromLabel(dto.getTypename()))
                .duration(dto.getDuration())
                .user(user)
                .build();

        // 3. SuggestionTourist 생성
        for (CourseResponseDto.CourseDayDto day : dto.getDays()) {
            for (CourseResponseDto.TouristInfoDto tDto : day.getList()) {
                SuggestionTourist st = new SuggestionTourist();
                // n일차
                st.defineDate(day.getDate());
                // 방문 시간
                st.defineTime(LocalTime.parse(tDto.getTime(), DateTimeFormatter.ofPattern("HH:mm")));

                Tourist tourist = touristService.findTourist(tDto.getTouristId());
                st.addTourist(tourist);
                suggestion.addSuggestionTourist(st);
            }
        }
        suggestionRepository.save(suggestion);

        // 5. 최종 저장
        return suggestion.getId();
    }

    public CourseResponseDto getLikedCourseDetail(Long courseId) {
        User user = authService.findUser().getUser();
        Suggestion suggestion = getSuggestion(courseId);
        if (!suggestion.getUser().equals(user))
            throw new BaseException(COURSE_NOT_FOUND);

        return CourseResponseDto.fromSuggestion(suggestion);
    }

    public void deleteCourse(Long courseId) {
        User user = authService.findUser().getUser();
        Suggestion suggestion = getSuggestion(courseId);
        if (!suggestion.getUser().equals(user))
            throw new BaseException(COURSE_NOT_FOUND);
        suggestionRepository.delete(suggestion);
    }

    @Transactional(readOnly = true)
    public List<AdditionRecommendsResponseDto> additionRecommends(Long courseId) {
        User user = authService.findUser().getUser();
        Suggestion suggestion = getSuggestion(courseId);
        if (!suggestionRepository.existsByUserAndId(user, courseId))
            throw new BaseException(COURSE_NOT_FOUND);
        return touristService.getRecommendsOnAddition(suggestion.getWellnessType().getCategories(),
                        stRepository.findDistinctRegionsBySuggestionId(suggestion.getId()))
                .stream().map(AdditionRecommendsResponseDto::toDto).collect(Collectors.toList());
    }

    public void modifyCourse(CourseUpdateRequestDto dto) {
        User user = authService.findUser().getUser();

        // 1. 기존 Suggestion 조회
        Suggestion suggestion = getSuggestion(dto.getCourseId());

        // 2. 기존 엔티티 맵핑 (SuggestionKey 활용)
        Map<SuggestionSortKey, SuggestionTourist> existingTourists = suggestion.getSuggestionTourists()
                .stream()
                .collect(Collectors.toMap(
                        st -> new SuggestionSortKey(st.getTourist().getId(), st.getDate(), st.getTime()),
                        Function.identity()
                ));

        Map<SuggestionSortKey, SuggestionCustomTourist> existingCustoms = suggestion.getSuggestionCustomTourists()
                .stream()
                .collect(Collectors.toMap(
                        sct -> new SuggestionSortKey(sct.getCustomTourist().getId(), sct.getDate(), sct.getTime()),
                        Function.identity()
                ));

        // 3. 삭제 후보 리스트
        List<SuggestionTourist> toDeleteTourists = new ArrayList<>(suggestion.getSuggestionTourists());
        List<SuggestionCustomTourist> toDeleteCustoms = new ArrayList<>(suggestion.getSuggestionCustomTourists());

        // 4. DTO 반복 처리
        for (CourseUpdateRequestDto.CourseDayDto dayDto : dto.getDays()) {
            for (CourseUpdateRequestDto.TouristInfoDto tDto : dayDto.getList()) {
                LocalTime time = LocalTime.parse(tDto.getTime());
                SuggestionSortKey key = new SuggestionSortKey(tDto.getTouristId(), dayDto.getDate(), time);

                if ("CUSTOM".equals(tDto.getType())) {
                    if (tDto.getTouristId() == -1) {
                        // 새 CustomTourist 생성
                        CustomTourist customTourist = CustomTourist.builder()
                                .name(tDto.getTitle())
                                .address(tDto.getAddress())
                                .region(RegionUtils.convertProvince(tDto.getAddress()))
                                .latitude(tDto.getLatitude())
                                .longitude(tDto.getLongitude())
                                .tel(tDto.getTel())
                                .build();
                        customTouristRepository.save(customTourist);

                        SuggestionCustomTourist sct = SuggestionCustomTourist.builder()
                                .customTourist(customTourist)
                                .suggestion(suggestion)
                                .date(dayDto.getDate())
                                .time(time)
                                .build();
                        suggestion.getSuggestionCustomTourists().add(sct);
                    } else {
                        SuggestionCustomTourist sct = existingCustoms.get(key);
                        if (sct != null) {
                            sct.defineDate(dayDto.getDate());
                            sct.defineTime(time);
                            toDeleteCustoms.remove(sct);
                        }
                    }

                } else if ("TOURIST".equals(tDto.getType()) && tDto.getTouristId() != -1) {
                    SuggestionTourist st = existingTourists.get(key);
                    if (st != null) {
                        // 날짜/시간 변경
                        st.defineDate(dayDto.getDate());
                        st.defineTime(time);
                        toDeleteTourists.remove(st);
                    } else {
                        // 새로운 SuggestionTourist 생성
                        Tourist tourist = touristService.findTourist(tDto.getTouristId());
                        SuggestionTourist newSt = SuggestionTourist.builder()
                                .suggestion(suggestion)
                                .tourist(tourist)
                                .date(dayDto.getDate())
                                .time(time)
                                .build();
                        suggestion.getSuggestionTourists().add(newSt);
                    }
                }
            }
        }

        // 5. 삭제 처리
        suggestion.getSuggestionTourists().removeAll(toDeleteTourists);
        suggestion.getSuggestionCustomTourists().removeAll(toDeleteCustoms);

        // 6. 저장
        suggestionRepository.save(suggestion);
    }


    @Transactional(readOnly = true)
    public List<LikedCourseResponseDto> getLikedCourseList() {
        List<LikedCourseResponseDto> result = new ArrayList<>();
        User user = authService.findUser().getUser();
        for (Suggestion likes : user.getSuggestions()) {
            SuggestionTourist st = stRepository.findTop1BySuggestionOrderByTimeAsc(likes);
            result.add(LikedCourseResponseDto.toDto(likes, st));
        }
        return result;
    }

    @Transactional(readOnly = true)
    public CourseResponseDto sharedCourse(Long courseId, String token) {

        Suggestion suggestion = getSuggestion(courseId);
        if (!suggestion.getToken().equals(token))
            throw new BaseException(COURSE_NOT_FOUND);
        return CourseResponseDto.fromSuggestion(suggestion);
    }

    public void changeCourseTitle(ChangeTitleRequestDto requestDto) {
        Suggestion suggestion = getSuggestion(requestDto.getCourseId());
        if (!suggestion.getUser().equals(authService.findUser().getUser()))
            throw new BaseException(COURSE_NOT_FOUND);
        suggestion.changeTitle(requestDto.getTitle());
    }

    private Suggestion getSuggestion(Long courseId) {
        return suggestionRepository.findById(courseId).orElseThrow(() -> new BaseException(COURSE_NOT_FOUND));
    }
}
