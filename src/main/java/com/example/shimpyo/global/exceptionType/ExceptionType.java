package com.example.shimpyo.global.exceptionType;

import org.springframework.http.HttpStatus;

public interface ExceptionType {
    String name();

    HttpStatus httpStatus();

    String message();
}
