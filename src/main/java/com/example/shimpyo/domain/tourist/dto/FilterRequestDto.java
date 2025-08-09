package com.example.shimpyo.domain.tourist.dto;


import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalTime;
import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class FilterRequestDto {
    private String region;
    private boolean reservationRequired;
    private String visitTime;
    private String requiredService;
    private String gender;
    private String ageGroup;
}
