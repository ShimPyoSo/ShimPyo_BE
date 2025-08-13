package com.example.shimpyo.domain.survey.entity;

import com.example.shimpyo.domain.tourist.entity.Category;
import lombok.Getter;

import java.util.Arrays;
import java.util.List;

@Getter
public enum WellnessType {
    비우는쉼표(Category.명상, Category.자연친화),
    땀흘리는쉼표(Category.명상, Category.자연친화),
    어울리는쉼표(Category.명상, Category.건강식),
    채우는쉼표(Category.건강식, Category.자연친화),
    피어나는쉼표(Category.K뷰티),
    숨쉬는쉼표(Category.자연친화),
    이완하는쉼표(Category.스파),
    이것저것쉼표(Category.전체);

    private final List<Category> categories;

    WellnessType(Category... categories) {
        this.categories = Arrays.asList(categories);
    }
}
