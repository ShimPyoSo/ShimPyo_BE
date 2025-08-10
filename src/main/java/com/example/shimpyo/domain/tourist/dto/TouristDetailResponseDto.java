package com.example.shimpyo.domain.tourist.dto;

import com.example.shimpyo.domain.tourist.entity.Tourist;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;

@Builder
@Data
@NoArgsConstructor
@AllArgsConstructor
public class TouristDetailResponseDto {

    // 관광지 id
    private Long id;
    // 관광지 이름
    private String title;
    // 지역
    private String region;
    // 주소
    private String address;
    // 오픈 시간
    private String openTime;
    // 마감 시간
    private String closeTime;
    // 전화번호
    private String tel;
    // 홈페이지
    private String homepage;
    // 예약 페이지
    private String reservation;
    // 관광지 사진 리스트
    private List<String> images = new ArrayList<>();
    // 경도
    private float latitude;
    // 위도
    private float longitude;

    public static TouristDetailResponseDto toDto(Tourist tourist, String region){
        List<String> image = new ArrayList<>();
        image.add(tourist.getImage());
        return TouristDetailResponseDto.builder()
                .id(tourist.getId())
                .title(tourist.getName())
                .region(region)
                .address(tourist.getAddress())
                .openTime(tourist.getOpenTime())
                .closeTime(tourist.getCloseTime())
                .tel(tourist.getTelNum())
                .homepage(tourist.getHomepageUrl())
                .reservation(tourist.getReservationUrl())
                .images(image)
                .latitude(tourist.getLatitude())
                .longitude(tourist.getLongitude())
                .build();
    }
}
