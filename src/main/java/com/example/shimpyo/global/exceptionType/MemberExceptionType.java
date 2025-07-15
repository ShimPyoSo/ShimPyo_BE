package com.example.shimpyo.global.exceptionType;

import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;

import static org.springframework.http.HttpStatus.BAD_REQUEST;

@RequiredArgsConstructor
public enum MemberExceptionType implements ExceptionType{


    MEMBER_NOT_FOUND(BAD_REQUEST, "회원이 존재하지 않습니다."),
    INVALID_VERIFICATION(BAD_REQUEST, "잘못된 이메일 인증 요청입니다."),
    INVALID_PROFILE_IMAGE(BAD_REQUEST, "잘못된 형식의 프로필 사진입니다."),
    EMAIL_DUPLICATION(BAD_REQUEST, "이미 가입된 이메일 입니다."),
    EMAIL_NOT_FOUNDED(BAD_REQUEST, "해당 이메일이 존재하지 않습니다."),
    NICKNAME_NOT_VALID(BAD_REQUEST, "닉네임 형식이 올바르지 않습니다."),
    NICKNAME_DUPLICATED(BAD_REQUEST, "중복된 닉네임입니다.");

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
