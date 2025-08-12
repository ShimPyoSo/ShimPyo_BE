package com.example.shimpyo.domain.survey.dto;

import com.example.shimpyo.domain.tourist.entity.Tourist;
import lombok.Builder;
import lombok.Getter;

import java.util.List;

@Getter
@Builder
public class CourseResponseDto {

    private Long courseId;
    private List<CourseDayDto> days;

    @Getter
    @Builder
    public static class CourseDayDto {
        private String date;
        private List<TouristInfoDto> list;
        public static CourseDayDto toDto(String date, List<TouristInfoDto> list) {
            return CourseDayDto.builder()
                    .date(date)
                    .list(list)
                    .build();
        }
    }

    @Getter
    @Builder
    public static class TouristInfoDto {
        private Long touristId;
        private String title;
        private String type;
        private String images;
        private String address;
        private OperationTimeDto operationTime;
        private Double latitude;
        private Double longitude;

        public static TouristInfoDto toDto(Tourist tourist) {
            return TouristInfoDto.builder()
                    .touristId(tourist.getId())
                    .title(tourist.getName())
                    .type(null)
                    .images(tourist.getImage())
                    .address(tourist.getAddress())
                    .operationTime(OperationTimeDto.toDto(tourist))
                    .latitude(tourist.getLatitude())
                    .longitude(tourist.getLongitude())
                    .build();
        }
    }

    @Getter
    @Builder
    public static class OperationTimeDto {
        private String dayOff;
        private String openTime;
        private String closeTime;
        private String breakTime;

        public static OperationTimeDto toDto(Tourist tourist) {
            return OperationTimeDto.builder()
                    .dayOff(tourist.getDayOff())
                    .openTime(tourist.getOpenTime())
                    .closeTime(tourist.getCloseTime())
                    .breakTime(tourist.getBreakTime())
                    .build();
        }
    }
}
