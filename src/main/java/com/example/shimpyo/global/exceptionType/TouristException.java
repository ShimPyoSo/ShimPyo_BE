package com.example.shimpyo.global.exceptionType;

import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;

@RequiredArgsConstructor
public enum TouristException implements ExceptionType{
    TOURIST_NOT_FOUND(HttpStatus.NOT_FOUND, "관광지 정보를 찾을 수 없습니다"),
    ILLEGAL_CATEGORY(HttpStatus.BAD_REQUEST, "잘못된 관광지 카테고리입니다."),
    REVIEW_NOT_FOUND(HttpStatus.NOT_FOUND, "리뷰 정보를 찾을 수 없습니다."),
    ILLEGAL_OFFER(HttpStatus.BAD_REQUEST, "잘못된 제공 서비스 유형입니다."),
    ILLEGAL_REGION(HttpStatus.BAD_REQUEST, "잘못된 지역 값입니다.");


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
