package com.example.shimpyo.domain.auth.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class InfoRequestDto {
    @NotNull(message = "성별은 필수입니다.")
    @Pattern(regexp = "^(female|male)$", message = "성별은 female 또는 male이어야 합니다.")
    private String gender;

    @Min(value = 1900, message = "출생년도는 1900년 이후여야 합니다.")
    @Max(value = 2025, message = "출생년도는 2025년 이전이어야 합니다.")
    private Integer birthYear;
}
