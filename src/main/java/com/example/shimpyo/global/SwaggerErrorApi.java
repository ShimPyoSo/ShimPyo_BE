package com.example.shimpyo.global;

import com.example.shimpyo.global.exceptionType.ExceptionType;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface SwaggerErrorApi {

    Class<? extends ExceptionType>[] type(); // enum 타입

    String[] codes();                      // 보여줄 상수 이름만 선택
}
