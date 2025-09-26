package com.example.shimpyo.domain.survey.service;

import com.example.shimpyo.domain.auth.service.AuthService;
import com.example.shimpyo.domain.auth.service.RedisService;
import com.example.shimpyo.domain.survey.dto.CourseRequestDto;
import com.example.shimpyo.domain.survey.dto.CourseResponseDto;
import com.example.shimpyo.domain.survey.entity.*;
import com.example.shimpyo.domain.tourist.entity.Category;
import com.example.shimpyo.domain.tourist.entity.Tourist;
import com.example.shimpyo.domain.tourist.service.TouristService;
import com.example.shimpyo.domain.user.entity.User;
import com.example.shimpyo.domain.utils.RegionUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalTime;
import java.util.*;

@Service
@Transactional
@RequiredArgsConstructor
public class SurveyService {

    private final AuthService authService;
    private final RedisService redisService;
    private final TouristService touristService;

    private final Random random = new Random();

    public CourseResponseDto getCourse(CourseRequestDto requestDto) {
        User user = authService.findUser().getUser();

        String typename = requestDto.getTypename();
        int days = requestDto.getDuration() == null ? 2 : parseDays(requestDto.getDuration());

        // 1) regionKey(예: "강원도") 및 regionValues(예: ["강원"]) 결정
        Optional<List<String>> regionCandidates = RegionUtils.getRegions(requestDto.getRegion());
        List<String> regionValues;
        String regionKey;
        if (regionCandidates.isEmpty()) {
            Map.Entry<String, List<String>> randomRegion = RegionUtils.getRandomRegion();
            regionValues = randomRegion.getValue();
            regionKey = randomRegion.getKey();
        } else {
            regionValues = regionCandidates.get();
            regionKey = requestDto.getRegion();
        }

        // 2) regionValues 중에서 하나 선택 (예: "강원")
        String region = regionValues.get(random.nextInt(regionValues.size()));
        System.out.println("Slected Region: " + region);
        int mealCount = requestDto.getMeal() == null ? 2 : requestDto.getMeal();

        // WellnessType에서 Category enum 리스트를 반환한다고 가정
        WellnessType wellnessType = WellnessType.fromLabel(typename);
        List<Category> activityCategories = wellnessType.getCategories(); // List<Category>
        for (Category activityCategory : activityCategories) {
            System.out.println("Select Categories : " + activityCategory.name());
        }

        boolean startsWithMeal = mealCount == 3;
        String token = UUID.randomUUID().toString();

        // 3) 선택된 region(예: "강원")에서 regionDetail 목록(distinct) 조회
        List<String> regionDetails = touristService.findDistinctRegionDetailsByRegion(region);
        for (String regionDetail : regionDetails) {
            System.out.println("Region Details : " + regionDetail);
        }
        if (regionDetails == null || regionDetails.isEmpty()) {
            // fallback: region 자체를 하나의 regionDetail로 간주
            regionDetails = List.of(region);
        }

        // 4) 날마다 다른 regionDetail을 골라서 하루 코스 생성
        List<CourseResponseDto.CourseDayDto> dayDtos = new ArrayList<>();

        // regionDetail들을 섞어두고 하나씩 꺼내서 사용(중복 피함). 부족하면 다시 셔플해서 채움.
        List<String> availableDetails = new ArrayList<>(regionDetails);
        Collections.shuffle(availableDetails, random);

        for (int day = 1; day <= days; day++) {
            boolean found = false;
            String regionDetailForDay = null;
            List<Tourist> meals = Collections.emptyList();
            List<Tourist> activities = Collections.emptyList();

            // availableDetails가 비었으면 다시 채움
            if (availableDetails.isEmpty()) {
                availableDetails = new ArrayList<>(regionDetails);
                Collections.shuffle(availableDetails, random);
            }

            // candidate 찾기 루프
            while (!availableDetails.isEmpty()) {
                regionDetailForDay = availableDetails.remove(0);

                meals = touristService.getTouristsByRegionDetailAndCategoryAndCount(
                        region, regionDetailForDay, List.of(Category.건강식), mealCount);
                activities = touristService.getTouristsByRegionDetailAndCategoryAndCount(
                        region, regionDetailForDay, activityCategories, 3);

                if (!meals.isEmpty() || !activities.isEmpty()) {
                    found = true;
                    break;
                }
                // meals/activities 둘 다 비었으면 다음 regionDetail로 시도
            }

            // 그래도 못 찾으면 region 전체 fallback
            if (!found) {
                System.out.println("Tourists Not founded ===> RegionDetail sets to Region");
                regionDetailForDay = region; // region 자체를 하나의 regionDetail로 간주
                meals = touristService.getTouristsByRegionAndCategoryAndCount(List.of(region), List.of(Category.건강식), mealCount);
                activities = touristService.getTouristsByRegionAndCategoryAndCount(List.of(region), activityCategories, 3);
            }

            System.out.println("Day " + day + " regionDetail: " + regionDetailForDay);
            System.out.println("Selected Meal Tourists");
            for (Tourist meal : meals) {
                System.out.println(meal.getId() + "  " + meal.getName() + "  " + meal.getRegionDetail());
            }
            System.out.println("Selected Activity Tourists");
            for (Tourist act : activities) {
                System.out.println(act.getId() + "  " + act.getName() + "  " + act.getRegionDetail());
            }

            CourseResponseDto.CourseDayDto dayDto = generateDayCourse(day, meals, activities, mealCount, startsWithMeal);
            dayDtos.add(dayDto);
        }

        CourseResponseDto courseResponseDto = CourseResponseDto.builder()
                .title((requestDto.getDuration() == null ? "1박 2일" : requestDto.getDuration()) + " " + regionKey + " 여행")
                .typename(typename)
                .token(token)
                .duration(requestDto.getDuration())
                .days(dayDtos)
                .build();

        // Redis에 저장
        redisService.saveSuggestion(courseResponseDto, token, user.getId());

        return courseResponseDto;
    }

    /**
     * 하루 단위 코스 생성 (기존 generateCourseResponse의 하루 내부 로직 분리)
     */
    private CourseResponseDto.CourseDayDto generateDayCourse(
            int day,
            List<Tourist> meals,
            List<Tourist> activities,
            int mealCount,
            boolean startWithMeal) {

        // openTime 기준 정렬 (nulls first)
        meals.sort(Comparator.comparing(Tourist::getOpenTime, Comparator.nullsFirst(Comparator.naturalOrder())));
        activities.sort(Comparator.comparing(Tourist::getOpenTime, Comparator.nullsFirst(Comparator.naturalOrder())));

        int mealIndex = 0;
        int activityIndex = 0;
        LocalTime time = LocalTime.of(9, 0); // 하루 시작 시간

        List<CourseResponseDto.TouristInfoDto> touristInfos = new ArrayList<>();
        int totalSlots = mealCount + 3; // 식사 수 + 활동 3회
        Set<Long> usedToday = new HashSet<>();

        Tourist lastCandidate = null;
        for (int slot = 0; slot < totalSlots; slot++) {
            boolean isMealTurn = startWithMeal == (slot % 2 == 0);
            Tourist candidate = null;
            LocalTime visitTime = time;

            if (isMealTurn) {
                candidate = findNextAvailable(meals, mealIndex, usedToday, visitTime);
                if (candidate != null) {
                    mealIndex = meals.indexOf(candidate) + 1;
                    time = time.plusHours(1);
                } else {
                    candidate = findNextAvailable(activities, activityIndex, usedToday, visitTime);
                    if (candidate != null) {
                        activityIndex = activities.indexOf(candidate) + 1;
                        time = time.plusHours(2);
                    }
                }
            } else {
                candidate = findNextAvailable(activities, activityIndex, usedToday, visitTime);
                if (candidate != null) {
                    activityIndex = activities.indexOf(candidate) + 1;
                    time = time.plusHours(2);
                } else {
                    candidate = findNextAvailable(meals, mealIndex, usedToday, visitTime);
                    if (candidate != null) {
                        mealIndex = meals.indexOf(candidate) + 1;
                        time = time.plusHours(1);
                    }
                }
            }

            if (candidate != null) {
                touristInfos.add(CourseResponseDto.TouristInfoDto.toDto(candidate, visitTime));
                usedToday.add(candidate.getId());
                lastCandidate = candidate;
            }
        }

        // 🔽 마지막에 "스테이" 체크 로직 추가
        if (!touristInfos.isEmpty()) {
            // 마지막 관광지가 "스테이"가 아니면 추가
            if (!lastCandidate.getTouristCategories().contains(Category.스테이)) {
                String regionDetail = lastCandidate.getRegionDetail();
                String region = lastCandidate.getRegion();
                Tourist stayCandidate = touristService.findStayTourist(regionDetail, usedToday);
                if (stayCandidate != null) {
                    touristInfos.add(CourseResponseDto.TouristInfoDto.toDto(stayCandidate, time));
                    usedToday.add(stayCandidate.getId());
                }
            }
        }

        return CourseResponseDto.CourseDayDto.builder()
                .date(day + "일")
                .list(touristInfos)
                .build();
    }

    // 같은 카테고리 후보 중 방문 가능한 관광지를 찾아 반환
    private Tourist findNextAvailable(List<Tourist> candidates, int startIndex, Set<Long> usedToday, LocalTime visitTime) {
        if (candidates == null || candidates.isEmpty()) return null;
        for (int i = startIndex; i < candidates.size(); i++) {
            Tourist candidate = candidates.get(i);
            if (!usedToday.contains(candidate.getId()) && canVisitAt(candidate, visitTime)) {
                return candidate;
            }
        }
        return null;
    }

    private boolean canVisitAt(Tourist tourist, LocalTime visitTime) {
        if (tourist.getOpenTime() == null) return true;
        LocalTime open = tourist.getOpenTime();
        LocalTime close = tourist.getCloseTime();
        System.out.println("Now Time + " + visitTime + " ====== " + open + "   " + close + (!visitTime.isBefore(open) && !visitTime.isAfter(close)));
        return !visitTime.isAfter(close);
    }

    private int parseDays(String duration) {
        // "1박2일" -> "12" -> substring(1) -> "2"
        return Integer.parseInt(duration.replaceAll("[^0-9]", "").substring(1));
    }
}