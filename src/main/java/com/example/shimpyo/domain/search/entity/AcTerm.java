package com.example.shimpyo.domain.search.entity;

import jakarta.persistence.*;
import lombok.*;

import java.text.Normalizer;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@EqualsAndHashCode(of = "termId")
@ToString
@Entity
@Table(
    indexes = {
        @Index(name = "ix_ac_term_norm", columnList = "term_norm"),
        @Index(name = "ix_ac_term_choseong", columnList = "term_choseong")
    }
)
public class AcTerm {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long termId;

    /** 화면에 노출할 원문 텍스트 */
    @Column(nullable = false, length = 200)
    private String term;

    /** 초성 전용 컬럼 (예: "명상 체험" -> "ㅁㅅㅊㅎ") */
    @Column(length = 200)
    private String termChoseong;

    /** 검색용 정규화 텍스트 (소문자 + 공백/특수 제거 등) */
    @Column(nullable = false, length = 200)
    private String termNorm;

    /** 저장/수정 시 자동으로 termNorm/termChoseong 생성 */
    @PrePersist
    @PreUpdate
    public void normalize() {
        // null 방어
        if (this.term == null) this.term = "";

        this.termNorm = normalizeKo(term);
        this.termChoseong = extractChoseong(term);
    }

    /** 한글/영문/숫자만 남기고, NFKC 표준화 + 소문자 + 공백/특수 제거 */
    private static String normalizeKo(String s) {
        if (s == null) return "";
        String t = Normalizer.normalize(s, Normalizer.Form.NFKC);
        t = t.toLowerCase();
        // 필요에 따라 규칙 완화/강화 가능: 지금은 한글/영문/숫자만 유지
        t = t.replaceAll("[^\\p{IsHangul}\\p{IsAlphabetic}\\p{IsDigit}]", "");
        return t;
    }

    /** 문자열에서 한글 음절(가~힣)의 초성만 추출 */
    private static String extractChoseong(String s) {
        if (s == null) return "";
        StringBuilder sb = new StringBuilder(s.length());
        for (int i = 0; i < s.length(); i++) {
            char ch = s.charAt(i);
            if (ch >= HANGUL_BASE && ch <= HANGUL_LAST) {
                int code = ch - HANGUL_BASE;
                int choseongIndex = code / (21 * 28);
                sb.append(CHOSEONG_LIST[choseongIndex]);
            }
        }
        return sb.toString();
    }

    private static final char HANGUL_BASE = 0xAC00;
    private static final char HANGUL_LAST = 0xD7A3;
    private static final char[] CHOSEONG_LIST = {
            'ㄱ','ㄲ','ㄴ','ㄷ','ㄸ','ㄹ','ㅁ','ㅂ','ㅃ','ㅅ',
            'ㅆ','ㅇ','ㅈ','ㅉ','ㅊ','ㅋ','ㅌ','ㅍ','ㅎ'
    };


}
