package com.example.shimpyo.global.exceptionType;

import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;

@RequiredArgsConstructor
public enum CourseException implements ExceptionType {
    COURSE_NOT_FOUND(HttpStatus.NOT_FOUND, "코스 정보를 찾을 수 없습니다."),
    ALREADY_LIKED(HttpStatus.BAD_REQUEST, "이미 찜한 코스입니다."),
    ILLEGAL_WELLNESS(HttpStatus.BAD_REQUEST, "잘못된 웰니스 유형입니다."),
    NOT_MY_COURSE(HttpStatus.BAD_REQUEST, "잘못된 코스 정보입니다."),
    EXPIRED_COURSE(HttpStatus.BAD_REQUEST, "코스 정보를 확인할 수 없습니다.");

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
