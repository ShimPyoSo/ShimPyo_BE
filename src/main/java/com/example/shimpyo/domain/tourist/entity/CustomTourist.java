package com.example.shimpyo.domain.tourist.entity;

import com.example.shimpyo.domain.survey.entity.SuggestionCustomTourist;
import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.SuperBuilder;

import java.util.ArrayList;
import java.util.List;

@Entity
@Getter
@Table(name = "custom_tourist")
@SuperBuilder
@NoArgsConstructor
public class CustomTourist extends AbstractTourist {

    @OneToMany(mappedBy = "customTourist", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<SuggestionCustomTourist> suggestionCustomTourists = new ArrayList<>();
}
