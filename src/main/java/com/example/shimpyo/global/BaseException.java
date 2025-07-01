package com.example.shimpyo.global;

import com.example.shimpyo.global.exceptionType.ExceptionType;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
@Getter
public class BaseException extends RuntimeException{
    private final ExceptionType exceptionType;

    @Override
    public String getMessage() {
        return exceptionType.message();
    }
}
