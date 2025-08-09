package com.example.shimpyo.global.exceptionType;

import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;

@RequiredArgsConstructor
public enum TouristCategoryException implements ExceptionType{

    TOURIST_CATEGORY_EXCEPTION(HttpStatus.NOT_FOUND, "관광지 유형 정보를 찾을 수 없습니다");


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
