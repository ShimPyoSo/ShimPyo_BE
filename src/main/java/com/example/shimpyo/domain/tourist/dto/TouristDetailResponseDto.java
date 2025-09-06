package com.example.shimpyo.domain.tourist.dto;

import com.example.shimpyo.domain.tourist.entity.Offer;
import com.example.shimpyo.domain.tourist.entity.Tourist;
import com.example.shimpyo.domain.tourist.entity.TouristOffer;
import lombok.*;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Builder
@Getter
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
    // 전화번호
    private String tel;
    // 홈페이지
    private String homepage;
    // 예약 페이지
    private String reservation;
    // 관광지 사진 리스트
    private List<String> images = new ArrayList<>();
    // 경도
    private Double latitude;
    // 위도
    private Double longitude;

    private OperationTime operationTime;

    private Facilities facilities;

    private Boolean isLiked;

    @Getter
    @Builder
    public static class Facilities {
        private boolean parking;
        private boolean accessible;
        private boolean reservation;
        private boolean pet;
        private boolean child;
        private boolean wifi;

        public static Facilities convertToFacilities(List<TouristOffer> touristOffers) {
            Set<Offer> offers = touristOffers.stream()
                    .map(TouristOffer::getOffer)
                    .collect(Collectors.toSet());

            return Facilities.builder()
                    .parking(offers.contains(Offer.PARKING))
                    .accessible(offers.contains(Offer.ACCESSIBLE))
                    .reservation(offers.contains(Offer.RESERVATION))
                    .pet(offers.contains(Offer.PET))
                    .child(offers.contains(Offer.CHILD))
                    .wifi(offers.contains(Offer.WIFI))
                    .build();
        }

    }
    public static TouristDetailResponseDto toDto(Tourist tourist, boolean isLiked){
        List<String> image = new ArrayList<>();
        image.add(tourist.getImage());
        return TouristDetailResponseDto.builder()
                .id(tourist.getId())
                .title(tourist.getName())
                .region(tourist.getRegion())
                .address(tourist.getAddress())
                .operationTime(OperationTime.toDto(tourist))
                .tel(tourist.getTel())
                .homepage(tourist.getHomepageUrl())
                .reservation(tourist.getReservationUrl())
                .images(image)
                .latitude(tourist.getLatitude())
                .longitude(tourist.getLongitude())
                .facilities(Facilities.convertToFacilities(tourist.getTouristOffers()))
                .isLiked(isLiked)
                .build();
    }
}
