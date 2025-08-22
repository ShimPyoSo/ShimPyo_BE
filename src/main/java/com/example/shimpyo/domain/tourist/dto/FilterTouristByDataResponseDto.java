package com.example.shimpyo.domain.tourist.dto;

import com.example.shimpyo.domain.tourist.entity.Tourist;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class FilterTouristByDataResponseDto {
    private Long id;
    private String title;
    private String type;
    private String region;
    private String address;
    private OperationTime operationTime;
    private String image;
    private Boolean isLiked;

    public static FilterTouristByDataResponseDto from(Tourist tourist, Boolean isLiked) {
        return FilterTouristByDataResponseDto.builder()
                .id(tourist.getId())
                .title(tourist.getName())
//                .type(tourist)
                .address(tourist.getAddress())
                .region(tourist.getRegion())
                .operationTime(OperationTime.of(tourist))
                .image(tourist.getImage()) // 또는 getImages().get(0) 등
                .isLiked(isLiked)
                .build();
    }

    @Builder
    @AllArgsConstructor
    @NoArgsConstructor
    @Data
    static class OperationTime{
        private String dayOff;
        private String openTime;
        private String closeTime;
        private String breakTime;

        public static OperationTime of(Tourist tourist){
            return OperationTime.builder()
                    .dayOff(tourist.getDayOff())
                    .openTime(tourist.getOpenTime())
                    .closeTime(tourist.getCloseTime())
                    .breakTime(tourist.getBreakTime())
                    .build();
        }
    }
}
