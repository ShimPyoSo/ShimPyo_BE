package com.example.shimpyo.domain.tourist.entity;

import com.example.shimpyo.global.BaseException;
import com.example.shimpyo.global.exceptionType.TouristException;
import lombok.Getter;

import java.util.Arrays;

@Getter
public enum Offer {
    PARKING, WIFI, ACCESSIBLE, PET, CHILD, RESERVATION;

    public static Offer fromString(String offerName) {
        return Arrays.stream(Offer.values())
                .filter(offer -> offer.name().equalsIgnoreCase(offerName))
                .findFirst()
                .orElseThrow(() -> new BaseException(TouristException.ILLEGAL_OFFER)); // 또는 적절한 예외
    }
}
