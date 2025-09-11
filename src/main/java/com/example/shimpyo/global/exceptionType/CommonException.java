package com.example.shimpyo.global.exceptionType;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;

@Getter
@RequiredArgsConstructor
public enum CommonException implements ExceptionType {
    ILLEGAL_ARGUMENT(HttpStatus.BAD_REQUEST, "ILLEGAL ARGUMENTS PROVIDED."),
    SERVER_ERROR(HttpStatus.INTERNAL_SERVER_ERROR, "Error Occurred During Processing.");


    private final HttpStatus status;
    private final String message;
    @Override
    public HttpStatus httpStatus() {
        return this.status;
    }

    @Override
    public String message() {
        return this.message;
    }
}
