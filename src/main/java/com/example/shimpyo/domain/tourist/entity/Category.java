package com.example.shimpyo.domain.tourist.entity;

import com.example.shimpyo.global.BaseException;
import com.example.shimpyo.global.exceptionType.TouristException;
import lombok.Getter;

import java.util.Arrays;

@Getter
public enum Category {
    명상("meditation"),
    스파("spa"),
    K뷰티("beauty"),
    자연친화("nature"),
    건강식("food");

    private final String code;

    Category(String code) {
        this.code = code;
    }

    public static Category fromCode(String code) {
        return Arrays.stream(Category.values())
                .filter(c -> c.code.equalsIgnoreCase(code)
                                   || c.name().equalsIgnoreCase(code))
                .findFirst()
                .orElseThrow(() -> new BaseException(TouristException.ILLEGAL_CATEGORY));
    }
}