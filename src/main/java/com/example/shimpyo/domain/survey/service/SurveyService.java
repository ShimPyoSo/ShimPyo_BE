package com.example.shimpyo.domain.survey.service;

import com.example.shimpyo.domain.auth.service.AuthService;
import com.example.shimpyo.domain.survey.dto.CourseRequestDto;
import com.example.shimpyo.domain.survey.dto.CourseResponseDto;
import com.example.shimpyo.domain.survey.entity.*;
import com.example.shimpyo.domain.survey.repository.SuggestionRepository;
import com.example.shimpyo.domain.survey.repository.SuggestionTouristRepository;
import com.example.shimpyo.domain.survey.repository.SuggestionUserRepository;
import com.example.shimpyo.domain.tourist.entity.Category;
import com.example.shimpyo.domain.tourist.entity.Tourist;
import com.example.shimpyo.domain.tourist.service.TouristService;
import com.example.shimpyo.domain.user.entity.User;
import com.example.shimpyo.global.BaseException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalTime;
import java.util.*;

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

    private static final Map<Integer, String> indexToKey = Map.of(
            0, "경상도",
            1, "전라도",
            2, "충청도",
            3, "수도권",
            4, "강원도",
            5, "제주도"
    );
    private static final Map<String, List<String>> regionMapping = Map.of(
            "경상도", List.of("경북", "경남"),
            "전라도", List.of("전북", "전남"),
            "충청도", List.of("충북", "충남"),
            "수도권", List.of("서울", "경기"),
            "강원도", List.of("강원"),
            "제주도", List.of("제주")
    );

    public CourseResponseDto getCourse(CourseRequestDto requestDto) {

        User user = authService.findUser().getUser();
        // 2. 기간, 식사 횟수 파싱
        String typename = requestDto.getTypename();
        int days = requestDto.getDuration() == null? 2 : parseDays(requestDto.getDuration());
        Optional<List<String>> regionCandidates = Optional.ofNullable(requestDto.getRegion())
                .map(regionMapping::get);
        List<String> regions;
        String region;
        if (regionCandidates.isEmpty()) {
            Random random = new Random();
            int index = random.nextInt(regionMapping.values().size());
            regions = new ArrayList<>(regionMapping.values()).get(index);
            region = indexToKey.get(index);
        } else {
            regions = regionCandidates.get();
            region = requestDto.getRegion();
        }

        int mealCount = requestDto.getMeal() == null? 2 : requestDto.getMeal();

        // 1. 유형 → 카테고리
        WellnessType wellnessType = WellnessType.fromLabel(typename);
        List<Category> categories = wellnessType.getCategories();

        // 3. 관광지 필터링
        List<Tourist> meals = touristService.getTouristsByRegionAndCategoryAndCount(regions, List.of(Category.건강식),
                mealCount * days);
        System.out.println("Meals");
        for (Tourist meal : meals) {
            System.out.println(meal.getName());
        }
        System.out.println("acts");
        List<Tourist> activities = touristService.getTouristsByRegionAndCategoryAndCount(regions, categories, 3 * days);
        for (Tourist activity : activities) {
            System.out.println(activity.getName());
        }
        // 4. 일정 생성
        boolean startsWithMeal = mealCount == 3;
        Suggestion suggestion = makeSuggestion(requestDto.getDuration() == null ? "1박 2일" : requestDto.getDuration(),
                region, user, wellnessType);
        Map<String, List<Tourist>> result = generateCourseWithSchedule(suggestion, meals, activities, days, mealCount, startsWithMeal);


        // 5. 응답 생성
        return CourseResponseDto.toDto(suggestion, CourseResponseDto.fromSuggestionTourists(suggestion.getSuggestionTourists()));
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

    public Map<String, List<Tourist>> generateCourseWithSchedule(Suggestion suggestion,
            List<Tourist> meals, List<Tourist> activities,
            int days, int mealCount, boolean startWithMeal) {

        Map<String, List<Tourist>> dayToCourse = new LinkedHashMap<>();

        // 인덱스 관리
        int mealIndex = 0;
        int activityIndex = 0;

        for (int day = 1; day <= days; day++) {
            List<Tourist> todayList = new ArrayList<>();
            LocalTime time = LocalTime.of(9, 0);  // 시작 시간 9시
            LocalTime visitTime = time;
            int totalSlots = mealCount + 3;  // 하루 slot 총 갯수 식사+활동 합

            for (int slot = 0; slot < totalSlots; slot++) {
                boolean isMealTurn = startWithMeal == (slot % 2 == 0);
                System.out.println("Now slot" + slot);
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
                    todayList.add(candidate);
                }
            }
            dayToCourse.put(day + "일", todayList);
        }
        return dayToCourse;
    }

    private boolean canVisitAt(Tourist tourist, LocalTime visitTime) {
        // 예시: tourist.openTime은 "08:00" 형식이라고 가정
        if (tourist.getOpenTime() == null) return true;

        LocalTime open = LocalTime.parse(tourist.getOpenTime());
        LocalTime close = LocalTime.parse(tourist.getCloseTime());

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
        if (!suggestion.getUser().getId().equals(user.getId()))
            throw new BaseException(COURSE_NOT_FOUND);

        return CourseResponseDto.toDto(suggestion,
                CourseResponseDto.fromSuggestionTourists(suggestion.getSuggestionTourists()));
    }
}