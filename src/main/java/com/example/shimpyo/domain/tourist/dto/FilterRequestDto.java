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
    private Long lastId;
    private String region;
    private String visitTime;
    private String facilities ;
    private String gender;
    private String ageGroup;

    @NotNull
    private String sortBy;
}
