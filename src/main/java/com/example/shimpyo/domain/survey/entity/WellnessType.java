package com.example.shimpyo.domain.survey.entity;

import com.example.shimpyo.domain.tourist.entity.Category;
import com.example.shimpyo.global.BaseException;
import com.example.shimpyo.global.exceptionType.CourseException;
import lombok.Getter;

import java.util.Arrays;
import java.util.List;

@Getter
public enum WellnessType {
    비우는쉼표("비우는 쉼표", Category.명상, Category.자연친화),
    땀흘리는쉼표("땀흘리는 쉼표", Category.명상, Category.자연친화),
    피어나는쉼표("피어나는 쉼표", Category.K뷰티),
    숨쉬는쉼표("숨쉬는 쉼표", Category.자연친화),
    이완하는쉼표("이완하는 쉼표", Category.스파),
    이것저것쉼표("이것저것 쉼표", Category.명상, Category.자연친화, Category.K뷰티, Category.스파);

    private final String label; // 프론트에 보여줄 이름
    private final List<Category> categories;

    WellnessType(String label, Category... categories) {
        this.label = label;
        this.categories = Arrays.asList(categories);
    }

    public static WellnessType fromLabel(String label) {
        return Arrays.stream(values())
                .filter(w -> w.label.equals(label))
                .findFirst()
                .orElseThrow(() -> new BaseException(CourseException.ILLEGAL_WELLNESS));
    }
}
