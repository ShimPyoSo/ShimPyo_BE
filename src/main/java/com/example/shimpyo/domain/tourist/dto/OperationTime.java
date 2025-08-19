package com.example.shimpyo.domain.tourist.dto;

import com.example.shimpyo.domain.tourist.entity.Tourist;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class OperationTime {
    private String dayOff;
    private String openTime;
    private String closeTime;
    private String breakTime;

    public static OperationTime toDto(Tourist tourist) {
        return OperationTime.builder()
                .dayOff(tourist.getDayOff())
                .openTime(tourist.getOpenTime())
                .closeTime(tourist.getCloseTime())
                .breakTime(tourist.getBreakTime())
                .build();
    }
}