package com.example.shimpyo.global.exceptionType;

import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;

@RequiredArgsConstructor
public enum TouristException implements ExceptionType{
    TOURIST_NOT_FOUND(HttpStatus.NOT_FOUND, "관광지 정보를 찾을 수 없습니다");


    private final HttpStatus status;
    private final String message;

    @Override
    public HttpStatus httpStatus() {
        return status;
    }

    @Override
    public String message() {
        return message;
    }
}
