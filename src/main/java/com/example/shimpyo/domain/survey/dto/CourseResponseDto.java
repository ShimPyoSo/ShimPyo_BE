package com.example.shimpyo.domain.survey.dto;

import com.example.shimpyo.domain.survey.entity.Suggestion;
import com.example.shimpyo.domain.survey.entity.SuggestionTourist;
import com.example.shimpyo.domain.tourist.dto.OperationTime;
import com.example.shimpyo.domain.tourist.entity.Tourist;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Builder;
import lombok.Getter;

import java.time.format.DateTimeFormatter;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.LinkedHashMap;
@Getter
@Builder
public class CourseResponseDto {

    @NotNull
    private Long courseId;
    @NotNull
    @NotBlank
    private String title;
    @NotNull
    @NotBlank
    private String typename;
    @NotNull
    @NotBlank
    private String token;
    @NotNull
    private List<CourseDayDto> days;

    public static CourseResponseDto toDto(Suggestion suggestion, List<CourseDayDto> days) {
        return CourseResponseDto.builder()
                .courseId(suggestion.getId())
                .title(suggestion.getTitle())
                .typename(suggestion.getWellnessType().getLabel())
                .token(suggestion.getToken())
                .days(days)
                .build();
    }

    public static CourseResponseDto fromSuggestion(Suggestion suggestion) {
        // 1. Suggestion이 가진 suggestionTourists 꺼내기
        List<SuggestionTourist> suggestionTourists = suggestion.getSuggestionTourists();

        // 2. 날짜별 그룹핑 + TouristInfoDto 변환
        Map<String, List<TouristInfoDto>> groupedByDate = suggestionTourists.stream()
                .collect(Collectors.groupingBy(
                        SuggestionTourist::getDate,
                        LinkedHashMap::new, // 순서 유지
                        Collectors.mapping(TouristInfoDto::toDto, Collectors.toList())
                ));

        // 3. CourseDayDto 리스트 만들기 (date 기준)
        List<CourseDayDto> days = groupedByDate.entrySet().stream()
                .map(entry -> CourseDayDto.builder()
                        .date(entry.getKey())
                        .list(entry.getValue())
                        .build())
                .collect(Collectors.toList());

        // 4. 최종 CourseResponseDto 생성
        return CourseResponseDto.builder()
                .courseId(suggestion.getId())
                .title(suggestion.getTitle())
                .typename(suggestion.getWellnessType().toString())
                .token(suggestion.getToken())
                .days(days)
                .build();
    }


    public static List<CourseDayDto> fromSuggestionTourists(List<SuggestionTourist> suggestionTourists) {
        Map<String, List<SuggestionTourist>> groupedByDate = suggestionTourists.stream()
                .collect(Collectors.groupingBy(
                        SuggestionTourist::getDate,
                        LinkedHashMap::new,
                        Collectors.collectingAndThen(
                                Collectors.toList(),
                                list -> list.stream()
                                        .sorted(Comparator.comparing(SuggestionTourist::getTime))
                                        .collect(Collectors.toList())
                        )
                ));

        return groupedByDate.entrySet().stream()
                .map(entry -> CourseDayDto.builder()
                        .date(entry.getKey())
                        .list(entry.getValue().stream()
                                .map(TouristInfoDto::toDto)
                                .collect(Collectors.toList()))
                        .build())
                .collect(Collectors.toList());
    }
    @Getter
    @Builder
    public static class CourseDayDto {
        @NotNull
        @NotBlank
        private String date;
        @NotNull
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
        @NotNull
        @NotBlank
        private String title;
        @NotNull
        @NotBlank
        private String time;
        private String images;
        @NotNull
        @NotBlank
        private String address;
        private OperationTime operationTime;
        @NotNull
        private Double latitude;
        @NotNull
        private Double longitude;

        public static TouristInfoDto toDto(SuggestionTourist st) {
            Tourist tourist = st.getTourist();
            return TouristInfoDto.builder()
                    .touristId(tourist.getId())
                    .title(tourist.getName())
                    .time(st.getTime().format(DateTimeFormatter.ofPattern("HH:mm")))
                    .images(tourist.getImage())
                    .address(tourist.getAddress())
                    .operationTime(OperationTime.toDto(tourist))
                    .latitude(tourist.getLatitude())
                    .longitude(tourist.getLongitude())
                    .build();
        }
    }
}
