package com.example.shimpyo.global.exceptionType;

import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;

@RequiredArgsConstructor
public enum AuthException implements ExceptionType{
    UNAUTHENTICATED_USER(HttpStatus.UNAUTHORIZED, "인가되지 않은 사용자입니다."),
    FORBIDDEN(HttpStatus.FORBIDDEN, "권한이 없습니다."),
    MAIL_CODE_NOT_MATCHED(HttpStatus.BAD_REQUEST, "인증 코드가 일치하지 않습니다."),
    MEMBER_INFO_NOT_MATCHED(HttpStatus.BAD_REQUEST, "아이디/패스워드가 잘못되었습니다."),
    PASSWORD_NOT_MATCHED(HttpStatus.BAD_REQUEST, "비밀번호가 일치하지 않습니다."),
    LOGIN_ID_DUPLICATION(HttpStatus.BAD_REQUEST, "중복된 아이디가 존재합니다."),
    USERNAME_NOT_VALIDATE(HttpStatus.BAD_REQUEST, "아이디가 유효하지 않습니다.[영 소문자 + 숫자 (6~12자)]"),
    INVALID_EMAIL_REQUEST(HttpStatus.BAD_REQUEST, "이메일이 존재하지 않습니다."),
    TWO_PASSWORD_NOT_MATCHED(HttpStatus.BAD_REQUEST, "새로 입력한 비밀번호가 일치하지 않습니다."),
    PASSWORD_DUPLICATED(HttpStatus.BAD_REQUEST, "기존 비밀번호와 동일한 비밀번호입니다."),
    AUTHENTICATION_GET_FAILED(HttpStatus.CONFLICT, "사용자 인증 정보가 없습니다."),
    SOCIAL_USER_CANT_CHANGE_PASSWORD(HttpStatus.FORBIDDEN, "소셜 유저는 패스워드 변경이 불가능합니다.");

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
