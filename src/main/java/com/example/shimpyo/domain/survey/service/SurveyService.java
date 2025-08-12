package com.example.shimpyo.domain.survey.service;

import com.example.shimpyo.domain.auth.service.AuthService;
import com.example.shimpyo.domain.survey.dto.CourseRequestDto;
import com.example.shimpyo.domain.survey.dto.CourseResponseDto;
import com.example.shimpyo.domain.survey.entity.Suggestion;
import com.example.shimpyo.domain.survey.entity.SuggestionTourist;
import com.example.shimpyo.domain.survey.entity.SurveyResult;
import com.example.shimpyo.domain.survey.entity.WellnessType;
import com.example.shimpyo.domain.survey.repository.SuggestionRepository;
import com.example.shimpyo.domain.survey.repository.SuggestionTouristRepository;
import com.example.shimpyo.domain.survey.repository.SurveyRepository;
import com.example.shimpyo.domain.tourist.entity.Category;
import com.example.shimpyo.domain.tourist.entity.Tourist;
import com.example.shimpyo.domain.tourist.service.TouristService;
import com.example.shimpyo.domain.user.entity.User;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@Transactional
@RequiredArgsConstructor
public class SurveyService {

    private final AuthService authService;
    private final TouristService touristService;
    private final SurveyRepository surveyRepository;
    private final SuggestionRepository suggestionRepository;
    private final SuggestionTouristRepository stRepository;

    private static final Map<String, List<String>> regionMapping = Map.of(
            "경상도", List.of("경북", "경남"),
            "전라도", List.of("전북", "전남"),
            "충청도", List.of("충북", "충남"),
            "수도권", List.of("서울", "경기")
    );

    public CourseResponseDto getCourse(CourseRequestDto requestDto) {

        User user = authService.findUser().getUser();
        // 2. 기간, 식사 횟수 파싱
        String typename = requestDto.getTypename();
        int days = requestDto.getDuration() == null? 2 : parseDays(requestDto.getDuration());
        List<String> regions = regionMapping.getOrDefault(requestDto.getRegion(), List.of(requestDto.getRegion()));  // 매핑 없으면 원래 값 그대로
        int mealCount = requestDto.getMeal() == null? 2 : requestDto.getMeal();

        // 1. 유형 → 카테고리
        WellnessType wellnessType = WellnessType.valueOf(typename.replace(" ", ""));
        List<Category> categories = wellnessType.getCategories();


        // 3. 관광지 필터링
        List<Tourist> candidates = touristService.findByRegionsAndCategories(regions, categories);

        // 4. 일정 생성
        List<CourseResponseDto.CourseDayDto> dayPlans = new ArrayList<>();
        boolean startWithMeal = (mealCount == 3);
        int totalSlots = startWithMeal? mealCount + 6 : mealCount + 5; // 예: 4, 5 등 실제 넣을 총 개수
        List<Tourist> tourList = new ArrayList<>();
        for (int day = 1; day <= days; day++) {
            List<Tourist> meals = pickRandomByCategory(candidates, categories, mealCount);
            List<Tourist> activities = pickRandomExceptCategory(candidates, categories, 3 - mealCount);
            int mealIndex = 0;
            int activityIndex = 0;

            List<CourseResponseDto.TouristInfoDto> todayList = new ArrayList<>();
            // 끼니 수에 따라 교차 순서 결정
            for (int i = 0; i < totalSlots; i++) {
                boolean isMealTurn = startWithMeal == (i % 2 == 0);

                if (isMealTurn) {
                    // 식당 넣기
                    if (mealIndex < meals.size()) {
                        tourList.add(meals.get(mealIndex));
                        todayList.add(toTouristInfoDto(meals.get(mealIndex++)));
                    } else if (activityIndex < activities.size()) {
                        // 식당 부족 시 활동으로 채움
                        tourList.add(activities.get(activityIndex));
                        todayList.add(toTouristInfoDto(activities.get(activityIndex++)));
                    }
                } else {
                    // 관광지 넣기
                    if (activityIndex < activities.size()) {
                        tourList.add(activities.get(activityIndex));
                        todayList.add(toTouristInfoDto(activities.get(activityIndex++)));
                    } else if (mealIndex < meals.size()) {
                        // 관광지 부족 시 식당으로 채움
                        tourList.add(meals.get(mealIndex));
                        todayList.add(toTouristInfoDto(meals.get(mealIndex++)));
                    }
                }
            }
            dayPlans.add(CourseResponseDto.CourseDayDto.toDto(day + "일차", todayList));
        }
        // 5. 응답 생성
        return CourseResponseDto.builder().courseId(makeCourse(requestDto, user, tourList)).days(dayPlans).build();
    }

    private Long makeCourse(CourseRequestDto requestDto, User user, List<Tourist> tourlist) {
        Suggestion suggestion = suggestionRepository.save(
                Suggestion.builder()
                        .title(requestDto.getDuration() + " " + requestDto.getRegion() + " 여행")
                        .surveyResult(saveSurveyResult(user))
                        .user(user)
                        .build());
        tourlist.forEach(t -> {
            SuggestionTourist st = stRepository.save(
                    SuggestionTourist.builder().suggestion(suggestion).tourist(t).build());
            suggestion.addSuggestionTourist(st);
            t.addSuggestionTourist(st);
        });
        return suggestion.getId();
    }

    private List<Tourist> pickRandomByCategory(List<Tourist> candidates, List<Category> categories, int count) {
        return candidates.stream()
                .filter(t -> t.getTouristCategories().stream()
                        .anyMatch(tc -> categories.contains(tc.getCategory())))
                .collect(Collectors.collectingAndThen(
                        Collectors.toList(),
                        list -> pickRandomFromList(list, count)
                ));
    }

    private List<Tourist> pickRandomExceptCategory(List<Tourist> candidates, List<Category> excluded, int count) {
        return candidates.stream()
                .filter(t -> t.getTouristCategories().stream()
                        .noneMatch(tc -> excluded.contains(tc.getCategory())))
                .collect(Collectors.collectingAndThen(
                        Collectors.toList(),
                        list -> pickRandomFromList(list, count)
                ));
    }

    private List<Tourist> pickRandomFromList(List<Tourist> list, int count) {
        Collections.shuffle(list);
        return list.stream().limit(count).collect(Collectors.toList());
    }

    private int parseDays(String duration) {
        return Integer.parseInt(duration.replaceAll("[^0-9]", "").substring(1)); // "1박2일" -> 2
    }
    private CourseResponseDto.TouristInfoDto toTouristInfoDto(Tourist tourists) {
        return CourseResponseDto.TouristInfoDto.toDto(tourists);
    }

    private SurveyResult saveSurveyResult(User user) {
        return surveyRepository.save(SurveyResult.builder().user(user).build());
    }
}