package com.example.shimpyo.domain.auth.dto;

import jakarta.validation.constraints.Email;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class FindPasswordRequestDto {

    private String username;
    @Email
    private String email;
}
