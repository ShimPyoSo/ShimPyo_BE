package com.example.shimpyo.domain.utils;

import java.util.ArrayList;
import java.util.List;

public class AutocompleteKeywordGenerator {

    private static final char[] CHO = {
            'ㄱ','ㄲ','ㄴ','ㄷ','ㄸ','ㄹ','ㅁ','ㅂ','ㅃ','ㅅ',
            'ㅆ','ㅇ','ㅈ','ㅉ','ㅊ','ㅋ','ㅌ','ㅍ','ㅎ'
    };

    private static char getChosung(char c) {
        if (c < 0xAC00 || c > 0xD7A3) return c;
        int code = c - 0xAC00;
        int choIdx = code / (21 * 28);
        return CHO[choIdx];
    }

    public static List<String> generate(String word) {
        List<String> results = new ArrayList<>();

        // 1. 초성 문자열
        StringBuilder chosung = new StringBuilder();
        for (char c : word.toCharArray()) {
            chosung.append(getChosung(c));
        }
        results.add(chosung.toString());

        // 2. 점진적 prefix (부분 문자열)
        for (int i = 1; i <= word.length(); i++) {
            results.add(word.substring(0, i));
        }

        // 3. 혼합형 (부분 + 초성)
        for (int i = 0; i < word.length(); i++) {
            StringBuilder mixed = new StringBuilder(word.substring(0, i));
            mixed.append(getChosung(word.charAt(i)));
            results.add(mixed.toString());
        }

        return results;
    }
}
