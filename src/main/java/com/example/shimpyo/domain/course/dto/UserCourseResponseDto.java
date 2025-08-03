package com.example.shimpyo.domain.course.dto;

import com.example.shimpyo.domain.course.entity.UserCourseDetail;
import com.example.shimpyo.domain.course.entity.UserCourseTourist;
import com.example.shimpyo.domain.tourist.entity.Tourist;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

@Builder
@Data
@AllArgsConstructor
@NoArgsConstructor
public class UserCourseResponseDto {
    // 여행 일차
    private String day;
    private List<CourseList> courseLists;

    // Entity -> DTO 변환 메서드
    public static UserCourseResponseDto from(UserCourseDetail userCourseDetail) {

        return UserCourseResponseDto.builder()
                .day(userCourseDetail.getDay() + "일차")
                .courseLists(
                        userCourseDetail.getUserCourseTourists()
                                .stream()
                                .map(t -> CourseList.from(t, userCourseDetail.getDate()))
                                .collect(Collectors.toList())
                )
                .build();

    }

    @Builder
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class CourseList{
        // 관광지 id
        private long id;
        // 미리보기 사진 url
        private String thumbnail_url;
        // 매장 타입 (카페,식당,관광지 등)
        private String content_type;
        // 경도
        private float longitude;
        // 위도
        private float latitude;
        // 관광지 방문 날짜
        private LocalDate visitDate;
        // 관광지 설명
        private String description;
        // 관광지 전화번호
        private String tel;
        // 관광지 운영 시간
        private String operation_time;
        // 관광지 예약 url
        private String reservation_url;
        // 관광지 휴식 시간
        private String break_time;

        public static CourseList from(UserCourseTourist userCourseTourist, LocalDate date){
            Tourist t = userCourseTourist.getTourist();

            return CourseList.builder()
                    .id(t.getId())
                    .thumbnail_url(t.getImage())
                    .content_type(t.getContent_id().toString())
                    .longitude(t.getLongitude())
                    .latitude(t.getLatitude())
                    .visitDate(date)
                    .description(t.getDescription())
                    .tel(t.getTel())
                    .operation_time(t.getOperationTime())
                    .reservation_url(t.getReservationUrl())
                    .break_time(t.getBreakTime())
                    .build();
        }
    }

}

