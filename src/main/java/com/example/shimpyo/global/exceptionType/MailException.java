package com.example.shimpyo.global.exceptionType;

import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;

import static org.springframework.http.HttpStatus.BAD_REQUEST;

@RequiredArgsConstructor
public enum MailException implements ExceptionType{
    MAIL_CODE_NOT_MATCHED(HttpStatus.BAD_REQUEST, "인증 코드가 일치하지 않습니다."),
    INVALID_EMAIL_REQUEST(HttpStatus.BAD_REQUEST, "이메일이 존재하지 않습니다."),
    EMAIL_NOT_VERIFIED(HttpStatus.BAD_REQUEST, "이메일 인증이 완료되지 않았습니다."),
    EMAIL_DUPLICATION(BAD_REQUEST, "이미 가입된 이메일 입니다."),
    EMAIL_NOT_FOUNDED(BAD_REQUEST, "해당 이메일이 존재하지 않습니다.");

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


