package com.example.shimpyo.domain.image.dto;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class ImageRequestDto {
    private String fileName;
    private Integer fileSize;
}
