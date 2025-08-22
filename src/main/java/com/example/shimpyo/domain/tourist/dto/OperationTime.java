package com.example.shimpyo.domain.tourist.dto;

import com.example.shimpyo.domain.tourist.entity.Tourist;
import lombok.Builder;
import lombok.Getter;

import java.time.format.DateTimeFormatter;

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
                .openTime(tourist.getOpenTime().format(DateTimeFormatter.ofPattern("HH:mm")))
                .closeTime(tourist.getCloseTime().format(DateTimeFormatter.ofPattern("HH:mm")))
                .breakTime(tourist.getBreakTime())
                .build();
    }
}