package com.example.shimpyo.domain.survey.service;

import com.example.shimpyo.domain.auth.service.AuthService;
import com.example.shimpyo.domain.course.dto.AdditionRecommendsResponseDto;
import com.example.shimpyo.domain.survey.dto.CourseRequestDto;
import com.example.shimpyo.domain.survey.dto.CourseResponseDto;
import com.example.shimpyo.domain.survey.entity.*;
import com.example.shimpyo.domain.survey.repository.SuggestionRepository;
import com.example.shimpyo.domain.survey.repository.SuggestionTouristRepository;
import com.example.shimpyo.domain.survey.repository.SuggestionUserRepository;
import com.example.shimpyo.domain.tourist.entity.Category;
import com.example.shimpyo.domain.tourist.entity.Tourist;
import com.example.shimpyo.domain.tourist.service.TouristService;
import com.example.shimpyo.domain.user.dto.LikedCourseResponseDto;
import com.example.shimpyo.domain.user.entity.User;
import com.example.shimpyo.global.BaseException;
import com.example.shimpyo.utils.RegionUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalTime;
import java.util.*;
import java.util.stream.Collectors;

import static com.example.shimpyo.global.exceptionType.CourseException.ALREADY_LIKED;
import static com.example.shimpyo.global.exceptionType.CourseException.COURSE_NOT_FOUND;

@Service
@Transactional
@RequiredArgsConstructor
public class SurveyService {

    private final AuthService authService;
    private final TouristService touristService;
    private final SuggestionRepository suggestionRepository;
    private final SuggestionTouristRepository stRepository;
    private final SuggestionUserRepository suRepository;

    public CourseResponseDto getCourse(CourseRequestDto requestDto) {

        User user = authService.findUser().getUser();
        // 2. 기간, 식사 횟수 파싱
        String typename = requestDto.getTypename();
        int days = requestDto.getDuration() == null? 2 : parseDays(requestDto.getDuration());
        Optional<List<String>> regionCandidates = RegionUtils.getRegions(requestDto.getRegion());
        List<String> regions;
        String region;
        if (regionCandidates.isEmpty()) {
            Map.Entry<String, List<String>> randomRegion = RegionUtils.getRandomRegion();
            regions = randomRegion.getValue();
            region = randomRegion.getKey();
        } else {
            regions = regionCandidates.get();
            region = requestDto.getRegion();
        }

        int mealCount = requestDto.getMeal() == null? 2 : requestDto.getMeal();

        // 1. 유형 → 카테고리
        WellnessType wellnessType = WellnessType.fromLabel(typename);
        List<String> categories = Category.toNameList(wellnessType.getCategories());

        // 3. 관광지 필터링
        List<Tourist> meals = touristService.getTouristsByRegionAndCategoryAndCount(regions, List.of(Category.건강식.name()),
                mealCount * days);
        List<Tourist> activities = touristService.getTouristsByRegionAndCategoryAndCount(regions, categories, 3 * days);

        // 4. 일정 생성
        boolean startsWithMeal = mealCount == 3;
        Suggestion suggestion = makeSuggestion(requestDto.getDuration() == null ? "1박 2일" : requestDto.getDuration(),
                region, user, wellnessType);
        generateCourseWithSchedule(suggestion, meals, activities, days, mealCount, startsWithMeal);


        // 5. 응답 생성
        return CourseResponseDto.fromSuggestion(suggestion);
    }

    private Suggestion makeSuggestion(String days, String region, User user, WellnessType type) {
        return suggestionRepository.save(
                Suggestion.builder()
                        .title(days + " " + region  + " 여행")
                        .token(UUID.randomUUID().toString())
                        .wellnessType(type)
                        .user(user)
                        .build());
    }

    public void generateCourseWithSchedule(Suggestion suggestion, List<Tourist> meals, List<Tourist> activities,
            int days, int mealCount, boolean startWithMeal) {

        // 인덱스 관리
        int mealIndex = 0;
        int activityIndex = 0;

        for (int day = 1; day <= days; day++) {
            LocalTime time = LocalTime.of(9, 0);  // 시작 시간 9시
            LocalTime visitTime = time;
            int totalSlots = mealCount + 3;  // 하루 slot 총 갯수 식사+활동 합

            for (int slot = 0; slot < totalSlots; slot++) {
                boolean isMealTurn = startWithMeal == (slot % 2 == 0);
                Tourist candidate = null;
                if (isMealTurn && mealIndex < meals.size()) {
                    Tourist mealCandidate = meals.get(mealIndex);
                    if (canVisitAt(mealCandidate, time)) {
                        candidate = mealCandidate;
                        mealIndex++;
                        time = time.plusHours(1);  // 식사 1시간
                    } else if (activityIndex < activities.size()) {
                        Tourist actCandidate = activities.get(activityIndex);
                        if (canVisitAt(actCandidate, time)) {
                            candidate = actCandidate;
                            activityIndex++;
                            time = time.plusHours(2); // 관광지 2시간
                        } else {
                            break; // 방문 불가시 다음 slot 진행 안함
                        }
                    } else {
                        break; // 더 이상 후보 없음
                    }
                } else if (!isMealTurn && activityIndex < activities.size()) {
                    Tourist actCandidate = activities.get(activityIndex);
                    if (canVisitAt(actCandidate, time)) {
                        candidate = actCandidate;
                        activityIndex++;
                        time = time.plusHours(2);
                    } else if (mealIndex < meals.size()) {
                        Tourist mealCandidate = meals.get(mealIndex);
                        if (canVisitAt(mealCandidate, time)) {
                            candidate = mealCandidate;
                            mealIndex++;
                            time = time.plusHours(1);
                        } else {
                            break;
                        }
                    } else {
                        break;
                    }
                }
                if (candidate != null) {
                    SuggestionTourist st = stRepository.save(SuggestionTourist.builder().suggestion(suggestion)
                                    .tourist(candidate).date(day + "일").time(visitTime).build());
                    suggestion.addSuggestionTourist(st);
                    candidate.addSuggestionTourist(st);
                }
            }
        }
    }

    private boolean canVisitAt(Tourist tourist, LocalTime visitTime) {
        // 예시: tourist.openTime은 "08:00" 형식이라고 가정
        if (tourist.getOpenTime() == null) return true;

        LocalTime open = tourist.getOpenTime();
        LocalTime close = tourist.getCloseTime();

        // 방문 시작 시간이 운영시간 내여야 방문 가능
        return !visitTime.isBefore(open) && !visitTime.isAfter(close);
    }

    private int parseDays(String duration) {
        return Integer.parseInt(duration.replaceAll("[^0-9]", "").substring(1)); // "1박2일" -> 2
    }

    public void likeCourse(Long courseId) {
        User user = authService.findUser().getUser();
        Suggestion suggestion = suggestionRepository.findById(courseId)
                .orElseThrow(() -> new BaseException(COURSE_NOT_FOUND));
        if (!suRepository.existsByUserAndSuggestion(user, suggestion))
            suRepository.save(SuggestionUser.builder().suggestion(suggestion).user(user).build());
        else throw new BaseException(ALREADY_LIKED);
    }

    public CourseResponseDto getLikedCourseDetail(Long courseId) {
        User user = authService.findUser().getUser();
        Suggestion suggestion = suggestionRepository.findById(courseId)
                .orElseThrow(() -> new BaseException(COURSE_NOT_FOUND));
        if (!suggestion.getUser().equals(user))
            throw new BaseException(COURSE_NOT_FOUND);

        return CourseResponseDto.fromSuggestion(suggestion);
    }

    public void deleteCourse(Long courseId) {
        User user = authService.findUser().getUser();
        Suggestion suggestion = suggestionRepository.findById(courseId)
                .orElseThrow(() -> new BaseException(COURSE_NOT_FOUND));
        if (!suggestion.getUser().equals(user))
            throw new BaseException(COURSE_NOT_FOUND);
        suggestionRepository.delete(suggestion);
    }

    @Transactional(readOnly = true)
    public List<AdditionRecommendsResponseDto> additionRecommends(Long courseId) {
        User user = authService.findUser().getUser();
        Suggestion suggestion = suggestionRepository.findById(courseId)
                .orElseThrow(() -> new BaseException(COURSE_NOT_FOUND));
        if (!suggestionRepository.existsByUserAndId(user, courseId))
            throw new BaseException(COURSE_NOT_FOUND);
        return touristService.getRecommendsOnAddition(suggestion.getWellnessType().getCategories(),
                        stRepository.findDistinctRegionsBySuggestionId(suggestion.getId()))
                .stream().map(AdditionRecommendsResponseDto::toDto).collect(Collectors.toList());
    }

    public void modifyCourse(CourseResponseDto requestDto) {
        User user = authService.findUser().getUser();
        Suggestion suggestion = suggestionRepository.findById(requestDto.getCourseId())
                .orElseThrow(() -> new BaseException(COURSE_NOT_FOUND));
        if (!suggestion.getUser().equals(user))
            throw new BaseException(COURSE_NOT_FOUND);


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

        Suggestion suggestion = suggestionRepository.findById(courseId).orElseThrow(() -> new BaseException(COURSE_NOT_FOUND));
        if (!suggestion.getToken().equals(token))
            throw new BaseException(COURSE_NOT_FOUND);
        return CourseResponseDto.fromSuggestion(suggestion);
    }
}