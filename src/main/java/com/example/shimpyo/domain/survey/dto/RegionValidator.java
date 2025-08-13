package com.example.shimpyo.domain.survey.dto;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

import java.util.Set;

public class RegionValidator implements ConstraintValidator<ValidRegion, String> {
    private static final Set<String> ALLOWED = Set.of(
            "강원도","수도권","경상도","전라도","제주도","충청도"
    );

    @Override
    public boolean isValid(String value, ConstraintValidatorContext context) {
        if (value == null) return true;   // null 허용
        if (value.isBlank()) return false; // 빈 문자열 불허
        return ALLOWED.contains(value);    // 허용 목록 체크
    }
}
