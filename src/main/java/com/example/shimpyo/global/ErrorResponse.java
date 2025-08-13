package com.example.shimpyo.global;

import com.example.shimpyo.global.exceptionType.ExceptionType;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
@AllArgsConstructor(access = AccessLevel.PROTECTED)
public class ErrorResponse {
    String name;
    int httpStatusCode;
    String message;

    public static ErrorResponse of(ExceptionType e){
        return ErrorResponse.builder()
                .name(e.name())
                .httpStatusCode(e.httpStatus().value())
                .message(e.message())
                .build();
    }

}
