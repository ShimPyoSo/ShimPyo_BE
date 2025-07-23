package com.example.shimpyo.global.exceptionType;

import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;

@RequiredArgsConstructor
public enum TokenException implements ExceptionType{
    INVALID_TOKEN(HttpStatus.UNAUTHORIZED, "잘못된 토큰입니다."),
    INVALID_REFRESH_TOKEN(HttpStatus.UNAUTHORIZED, "RefreshToken 유효하지 않음"),
    NOT_MATCHED_REFRESH_TOKEN(HttpStatus.UNAUTHORIZED, "RefreshToken 불일치"),
    TOKEN_IS_BLACKLISTED(HttpStatus.UNAUTHORIZED, "로그아웃 된 쿠키 토큰입니다.");

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
