package com.example.shimpyo.domain.user.dto;

import lombok.Builder;
import lombok.Getter;

import java.util.List;

@Getter
@Builder
public class SeenTouristRequestDto {
    private List<Integer> touristIds;
}
