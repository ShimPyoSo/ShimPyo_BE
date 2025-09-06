package com.example.shimpyo.domain.survey.service;

import com.example.shimpyo.domain.auth.service.AuthService;
import com.example.shimpyo.domain.auth.service.RedisService;
import com.example.shimpyo.domain.survey.dto.CourseRequestDto;
import com.example.shimpyo.domain.survey.dto.CourseResponseDto;
import com.example.shimpyo.domain.survey.entity.*;
import com.example.shimpyo.domain.survey.repository.SuggestionRepository;
import com.example.shimpyo.domain.survey.repository.SuggestionTouristRepository;
import com.example.shimpyo.domain.tourist.entity.Category;
import com.example.shimpyo.domain.tourist.entity.Tourist;
import com.example.shimpyo.domain.tourist.service.TouristService;
import com.example.shimpyo.domain.user.entity.User;
import com.example.shimpyo.domain.utils.RegionUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.*;

@Service
@Transactional
@RequiredArgsConstructor
public class SurveyService {

    private final AuthService authService;
    private final RedisService redisService;
    private final TouristService touristService;

    public CourseResponseDto getCourse(CourseRequestDto requestDto) {
        User user = authService.findUser().getUser();

        String typename = requestDto.getTypename();
        int days = requestDto.getDuration() == null ? 2 : parseDays(requestDto.getDuration());

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

        int mealCount = requestDto.getMeal() == null ? 2 : requestDto.getMeal();
        WellnessType wellnessType = WellnessType.fromLabel(typename);
        List<String> categories = Category.toNameList(wellnessType.getCategories());

        // 관광지 후보
        List<Tourist> meals = touristService.getTouristsByRegionAndCategoryAndCount(
                regions, List.of(Category.건강식.name()), mealCount * days);
        List<Tourist> activities = touristService.getTouristsByRegionAndCategoryAndCount(
                regions, categories, 3 * days);

        // 코스 생성
        boolean startsWithMeal = mealCount == 3;
        String token = UUID.randomUUID().toString();

        CourseResponseDto courseResponseDto = generateCourseResponse(
                typename,
                requestDto.getDuration() == null ? "1박 2일" : requestDto.getDuration() + " " + region + " 여행",
                token, meals, activities, days,  mealCount, startsWithMeal);

        // Redis에 저장
        redisService.saveSuggestion(courseResponseDto, token, user.getId());

        return courseResponseDto;
    }

    public CourseResponseDto generateCourseResponse(String typename, String title, String token,
                                                    List<Tourist> meals, List<Tourist> activities,
                                                    int days, int mealCount, boolean startWithMeal) {

        int mealIndex = 0;
        int activityIndex = 0;
        List<CourseResponseDto.CourseDayDto> dayDtos = new ArrayList<>();
        Set<Long> usedTouristIds = new HashSet<>(); // 이미 추가된 Tourist ID 관리

        for (int day = 1; day <= days; day++) {
            LocalTime time = LocalTime.of(9, 0); // 하루 시작 시간
            List<CourseResponseDto.TouristInfoDto> touristInfos = new ArrayList<>();
            int totalSlots = mealCount + 3;

            for (int slot = 0; slot < totalSlots; slot++) {
                boolean isMealTurn = startWithMeal == (slot % 2 == 0);
                Tourist candidate = null;
                LocalTime visitTime = time;

                if (isMealTurn && mealIndex < meals.size()) {
                    Tourist mealCandidate = meals.get(mealIndex);
                    if (canVisitAt(mealCandidate, time) && usedTouristIds.add(mealCandidate.getId())) {
                        candidate = mealCandidate;
                        mealIndex++;
                        time = time.plusHours(1);
                    } else if (activityIndex < activities.size()) {
                        Tourist actCandidate = activities.get(activityIndex);
                        if (canVisitAt(actCandidate, time) && usedTouristIds.add(actCandidate.getId())) {
                            candidate = actCandidate;
                            activityIndex++;
                            time = time.plusHours(2);
                        }
                    }
                } else if (!isMealTurn && activityIndex < activities.size()) {
                    Tourist actCandidate = activities.get(activityIndex);
                    if (canVisitAt(actCandidate, time) && usedTouristIds.add(actCandidate.getId())) {
                        candidate = actCandidate;
                        activityIndex++;
                        time = time.plusHours(2);
                    } else if (mealIndex < meals.size()) {
                        Tourist mealCandidate = meals.get(mealIndex);
                        if (canVisitAt(mealCandidate, time) && usedTouristIds.add(mealCandidate.getId())) {
                            candidate = mealCandidate;
                            mealIndex++;
                            time = time.plusHours(1);
                        }
                    }
                }

                if (candidate != null) {
                    touristInfos.add(CourseResponseDto.TouristInfoDto.toDto(candidate, visitTime));
                }
            }
            dayDtos.add(CourseResponseDto.CourseDayDto.builder()
                    .date(day + "일").list(touristInfos).build());
        }

        return CourseResponseDto.builder()
                .title(title)
                .typename(typename)
                .token(token)
                .days(dayDtos)
                .build();
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

}