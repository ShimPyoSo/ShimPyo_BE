package com.example.shimpyo.domain.utils;

import com.example.shimpyo.global.BaseException;
import com.example.shimpyo.global.exceptionType.TouristException;

import java.util.*;

public class RegionUtils {

    private static final Map<String, List<String>> regionMapping = Map.of(
            "경상도", List.of("경북", "경남"),
            "전라도", List.of("전북", "전남"),
            "충청도", List.of("충북", "충남"),
            "수도권", List.of("서울", "경기"),
            "강원도", List.of("강원"),
            "제주도", List.of("제주")
    );
    private static final Map<String, String> valueToSubRegion = Map.ofEntries(
            Map.entry("seoul", "서울"),
            Map.entry("gyeonggi", "경기"),
            Map.entry("gangwon", "강원"),
            Map.entry("jeju", "제주"),
            Map.entry("chungbuk", "충북"),
            Map.entry("chungnam", "충남"),
            Map.entry("jeonbuk", "전북"),
            Map.entry("jeonnam", "전남"),
            Map.entry("gyeongbuk", "경북"),
            Map.entry("gyeongnam", "경남"),

            // 특별 규칙
            Map.entry("busan", "경남"),
            Map.entry("ulsan", "경남"),
            Map.entry("daegu", "경북"),
            Map.entry("daejeon", "충남"),
            Map.entry("sejong", "충남"),
            Map.entry("incheon", "경기"),
            Map.entry("gwangju", "전남")
    );

    /**
     * 지역 키("수도권")로 하위 지역 리스트 반환
     */
    public static Optional<List<String>> getRegions(String regionKey) {
        if (regionKey == null) return Optional.empty();
        return Optional.ofNullable(regionMapping.get(regionKey));
    }

    /**
     * 하위 지역 리스트(["서울", "경기"])로 상위 지역 키("수도권") 반환
     */
    public static Optional<String> getRegionKey(List<String> regions) {
        return regionMapping.entrySet().stream()
                .filter(entry -> new HashSet<>(entry.getValue()).equals(new HashSet<>(regions)))
                .map(Map.Entry::getKey)
                .findFirst();
    }

    /**
     * 랜덤으로 지역 뽑기
     */
    public static Map.Entry<String, List<String>> getRandomRegion() {
        List<Map.Entry<String, List<String>>> entries = new ArrayList<>(regionMapping.entrySet());
        Random random = new Random();
        return entries.get(random.nextInt(entries.size()));
    }

    /**
     * "busan" -> "경남", "daegu" -> "경북"
     */
    public static String convertToRegion(String value) {
        return value == null? null : valueToSubRegion.get(value);
    }
}
