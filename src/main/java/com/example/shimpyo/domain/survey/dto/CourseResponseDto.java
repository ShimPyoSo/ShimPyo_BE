package com.example.shimpyo.domain.survey.dto;

import com.example.shimpyo.domain.survey.entity.Suggestion;
import com.example.shimpyo.domain.survey.entity.SuggestionCustomTourist;
import com.example.shimpyo.domain.survey.entity.SuggestionTourist;
import com.example.shimpyo.domain.tourist.entity.CustomTourist;
import com.example.shimpyo.domain.tourist.entity.Tourist;
import com.example.shimpyo.global.BaseException;
import com.example.shimpyo.global.exceptionType.CommonException;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

@Getter
@Builder
public class CourseResponseDto {

    private Long courseId;
    private String title;
    private String typename;
    private String token;
    private List<CourseDayDto> days;

    @Getter
    @Builder
    public static class CourseDayDto {
        private String date;
        private List<TouristInfoDto> list;
        public static CourseDayDto toDto(SuggestionTourist st, List<TouristInfoDto> list) {
            return CourseDayDto.builder()
                    .date(st.getDate())
                    .list(list)
                    .build();
        }
    }

    @Getter
    @Builder
    public static class TouristInfoDto {
        private Long touristId;
        private String title;
        private String time;
        private String images;
        private String address;
        private String tel;
        private Double latitude;
        private Double longitude;
        private String type;

        public static TouristInfoDto toDto(Tourist tourist, LocalTime visitTime) {
            return TouristInfoDto.builder()
                    .touristId(tourist.getId())
                    .title(tourist.getName())
                    .time(visitTime.format(DateTimeFormatter.ofPattern("HH:mm")))
                    .images(tourist.getImage())
                    .address(tourist.getAddress())
                    .tel(tourist.getTel())
                    .latitude(tourist.getLatitude())
                    .longitude(tourist.getLongitude())
                    .type("TOURIST")
                    .build();
        }

        // SuggestionTourist → DTO 변환
        public static TouristInfoDto toDto(SuggestionTourist st) {
            Tourist t = st.getTourist();
            return TouristInfoDto.builder()
                    .touristId(t.getId())
                    .title(t.getName())
                    .time(st.getTime().format(DateTimeFormatter.ofPattern("HH:mm")))
                    .images(t.getImage())
                    .address(t.getAddress())
                    .tel(t.getTel())
                    .latitude(t.getLatitude())
                    .longitude(t.getLongitude())
                    .type("TOURIST")
                    .build();
        }

        // SuggestionCustomTourist → DTO 변환
        public static TouristInfoDto toDto(SuggestionCustomTourist sct) {
            CustomTourist ct = sct.getCustomTourist();
            return TouristInfoDto.builder()
                    .touristId(ct.getId())
                    .title(ct.getName())
                    .time(sct.getTime().format(DateTimeFormatter.ofPattern("HH:mm")))
                    .images(ct.getImage())
                    .address(ct.getAddress())
                    .tel(ct.getTel())
                    .latitude(ct.getLatitude())
                    .longitude(ct.getLongitude())
                    .type("CUSTOM")
                    .build();
        }
    }

    // Suggestion → CourseResponseDto 변환
    public static CourseResponseDto fromSuggestion(Suggestion suggestion) {
        List<Object> allTourists = new ArrayList<>();
        allTourists.addAll(suggestion.getSuggestionTourists());
        allTourists.addAll(suggestion.getSuggestionCustomTourists());

        // 정렬: date("1일차") → time
        allTourists.sort(
                Comparator.comparingInt(t -> parseDay(getDate(t)))
                        .thenComparing(CourseResponseDto::getTime)
        );

        // 그룹핑: date → List<TouristInfoDto>
        Map<String, List<TouristInfoDto>> grouped = allTourists.stream()
                .collect(Collectors.groupingBy(
                        CourseResponseDto::getDate,
                        LinkedHashMap::new,
                        Collectors.mapping(CourseResponseDto::toTouristInfoDto, Collectors.toList())
                ));

        // CourseDayDto 변환
        List<CourseDayDto> days = grouped.entrySet().stream()
                .map(e -> CourseDayDto.builder()
                        .date(e.getKey())
                        .list(e.getValue())
                        .build())
                .toList();

        return CourseResponseDto.builder()
                .courseId(suggestion.getId())
                .title(suggestion.getTitle())
                .typename(suggestion.getWellnessType().getLabel())
                .token(suggestion.getToken())
                .days(days)
                .build();
    }

    private static int parseDay(String day) {
        return Integer.parseInt(day.replace("일", "").trim());
    }

    private static String getDate(Object obj) {
        if (obj instanceof SuggestionTourist st) return st.getDate();
        else if (obj instanceof SuggestionCustomTourist ct) return ct.getDate();
        throw new BaseException(CommonException.ILLEGAL_ARGUMENT);
    }

    private static LocalTime getTime(Object obj) {
        if (obj instanceof SuggestionTourist st) return st.getTime();
        else if (obj instanceof SuggestionCustomTourist ct) return ct.getTime();
        throw new BaseException(CommonException.ILLEGAL_ARGUMENT);
    }

    private static TouristInfoDto toTouristInfoDto(Object obj) {
        if (obj instanceof SuggestionTourist st) return TouristInfoDto.toDto(st);
        else if (obj instanceof SuggestionCustomTourist ct) return TouristInfoDto.toDto(ct);
        throw new BaseException(CommonException.ILLEGAL_ARGUMENT);
    }
}
