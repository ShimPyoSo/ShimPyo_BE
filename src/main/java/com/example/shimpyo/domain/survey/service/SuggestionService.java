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
import com.example.shimpyo.domain.survey.repository.SuggestionUserRepository;
import com.example.shimpyo.domain.tourist.entity.CustomTourist;
import com.example.shimpyo.domain.tourist.entity.Tourist;
import com.example.shimpyo.domain.tourist.service.TouristService;
import com.example.shimpyo.domain.user.dto.LikedCourseResponseDto;
import com.example.shimpyo.domain.user.entity.User;
import com.example.shimpyo.domain.utils.RegionUtils;
import com.example.shimpyo.global.BaseException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
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
    private final SuggestionUserRepository suRepository;
    private final SuggestionTouristRepository stRepository;
    private final SuggestionRepository suggestionRepository;
    private final CustomTouristRepository customTouristRepository;


    public Long likeCourse(String token) {
        User user = authService.findUser().getUser();
        SuggestionRedisDto dto = redisService.findSuggestionByTempId(token);
        System.out.println("Request Token : " + token);
        System.out.println("Request User : " + user.getId());
        System.out.println("Found Suggetion's User : " + dto.getUserId());
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
                .user(user)
                .build();

        // 3. SuggestionTourist 생성
        for (CourseResponseDto.CourseDayDto day : dto.getDays()) {
            for (CourseResponseDto.TouristInfoDto tDto : day.getList()) {
                SuggestionTourist st = new SuggestionTourist();
                st.addSuggestion(suggestion);

                // n일차
                st.defineDate(day.getDate());
                // 방문 시간
                st.defineTime(LocalTime.parse(tDto.getTime(), DateTimeFormatter.ofPattern("HH:mm")));

                if (tDto.getTouristId() != null) {
                    Tourist tourist = touristService.findTourist(tDto.getTouristId());
                    st.setTourist(tourist);
                }
                suggestion.addSuggestionTourist(st);
            }
        }

        suggestionRepository.save(suggestion);

        // 4. SuggestionUser 저장
        suRepository.save(SuggestionUser.builder()
                .user(user)
                .suggestion(suggestion)
                .build());


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

    @Transactional
    public void modifyCourse(CourseUpdateRequestDto dto) {
        User user = authService.findUser().getUser();

        // 1. 기존 Suggestion 조회
        Suggestion suggestion = getSuggestion(dto.getCourseId());

        // 2. 기존 엔티티 맵핑
        Map<Long, SuggestionTourist> existingTourists = suggestion.getSuggestionTourists()
                .stream().collect(Collectors.toMap(st -> st.getTourist().getId(), Function.identity()));
        Map<Long, SuggestionCustomTourist> existingCustoms = suggestion.getSuggestionCustomTourists()
                .stream().collect(Collectors.toMap(sct -> sct.getCustomTourist().getId(), Function.identity()));

        // 3. 업데이트할 리스트
        List<SuggestionTourist> updatedTourists = new ArrayList<>();
        List<SuggestionCustomTourist> updatedCustoms = new ArrayList<>();

        for (CourseUpdateRequestDto.CourseDayDto dayDto : dto.getDays()) {
            for (CourseUpdateRequestDto.TouristInfoDto tDto : dayDto.getList()) {

                // 3-1. 새 CustomTourist 생성
                if (tDto.getTouristId() == -1 && "CUSTOM".equals(tDto.getType())) {
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
                            .time(LocalTime.parse(tDto.getTime()))
                            .build();
                    updatedCustoms.add(sct);

                } else if ("TOURIST".equals(tDto.getType()) && tDto.getTouristId() != -1) {
                    // 3-2. 기존 Tourist 수정
                    SuggestionTourist st = existingTourists.get(tDto.getTouristId());
                    if (st != null) {
                        st.defineDate(dayDto.getDate());
                        st.defineTime(LocalTime.parse(tDto.getTime()));
                        updatedTourists.add(st);
                        existingTourists.remove(tDto.getTouristId());
                    }

                } else if ("CUSTOM".equals(tDto.getType()) && tDto.getTouristId() != -1) {
                    // 3-3. 기존 CustomTourist 수정
                    SuggestionCustomTourist sct = existingCustoms.get(tDto.getTouristId());
                    if (sct != null) {
                        sct.defineDate(dayDto.getDate());
                        sct.defineTime(LocalTime.parse(tDto.getTime()));
                        updatedCustoms.add(sct);
                        existingCustoms.remove(tDto.getTouristId());
                    }
                }
            }
        }

        // 4. 삭제 처리: existing에 남아있는 것들은 삭제
        suggestion.getSuggestionTourists().clear();
        suggestion.getSuggestionTourists().addAll(updatedTourists);

        suggestion.getSuggestionCustomTourists().clear();
        suggestion.getSuggestionCustomTourists().addAll(updatedCustoms);

        suggestionRepository.save(suggestion);
    }



    @Transactional(readOnly = true)
    public List<LikedCourseResponseDto> getLikedCourseList() {
        User user = authService.findUser().getUser();
        return user.getLikedSuggestion().stream()
                .map(s -> LikedCourseResponseDto.toDto(s.getSuggestion(),
                        s.getSuggestion().getSuggestionTourists().get(0).getTourist().getImage()))
                .collect(Collectors.toList());
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
