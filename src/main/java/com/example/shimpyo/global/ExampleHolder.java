package com.example.shimpyo.global;

import io.swagger.v3.oas.models.examples.Example;
import lombok.Builder;
import lombok.Getter;

import java.util.Map;

@Getter
@Builder
public class ExampleHolder {
    private Map<String, Example> holder;    // Swagger Example 객체
    private int code;          // HTTP 상태 코드
    private String name;       // 에러 코드 이름 (ex: “USER_404”)
}
