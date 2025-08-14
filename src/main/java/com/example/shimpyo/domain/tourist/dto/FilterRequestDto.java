package com.example.shimpyo.domain.tourist.dto;


import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

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

    @NotNull
    private String sortBy;
}
