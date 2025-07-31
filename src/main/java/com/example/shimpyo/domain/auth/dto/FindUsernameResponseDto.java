package com.example.shimpyo.domain.auth.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Builder
@AllArgsConstructor
@Data
@NoArgsConstructor
public class FindUsernameResponseDto {
    private String username;
    private LocalDate createdAt;
}
