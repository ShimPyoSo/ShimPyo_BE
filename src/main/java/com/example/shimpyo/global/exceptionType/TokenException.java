package com.example.shimpyo.global.exceptionType;

import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;

@RequiredArgsConstructor
public enum TokenException implements ExceptionType{
    INVALID_TOKEN(HttpStatus.UNAUTHORIZED, "잘못된 토큰입니다."),
    INVALID_REFRESH_TOKEN(HttpStatus.UNAUTHORIZED, "RefreshToken 유효하지 않음"),
    NOT_MATCHED_REFRESH_TOKEN(HttpStatus.UNAUTHORIZED, "RefreshToken 불일치"),
    TOKEN_IS_BLACKLISTED(HttpStatus.UNAUTHORIZED, "로그아웃 된 쿠키 토큰입니다."),
    INVALID_VISIT_TIME_FORMAT(HttpStatus.BAD_REQUEST, "잘못된 형식의 시간입니다."),
    UNSUPPORTED_GENDER(HttpStatus.BAD_REQUEST, "지원되지 않는 성별입니다."),
    UNSUPPORTED_AGE_GROUP(HttpStatus.BAD_REQUEST, "지원되지 않는 연령대입니다.");

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
