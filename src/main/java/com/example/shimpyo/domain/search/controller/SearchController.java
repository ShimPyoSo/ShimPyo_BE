package com.example.shimpyo.domain.search.controller;

import com.example.shimpyo.domain.search.dto.SuggestItem;
import com.example.shimpyo.domain.search.service.SuggestService;
import com.example.shimpyo.domain.tourist.dto.FilterRequestDto;
import com.example.shimpyo.domain.tourist.dto.FilterTouristByDataResponseDto;
import com.example.shimpyo.domain.tourist.service.TouristService;
import com.example.shimpyo.global.SwaggerErrorApi;
import com.example.shimpyo.global.exceptionType.TouristException;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Set;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/search")
public class SearchController {

    private final SuggestService suggestService;
    private final TouristService touristService;

    /** 자동완성 (2글자 이상부터 호출 권장) */
    @GetMapping(value = "/autocomplete")
    public ResponseEntity<Set<SuggestItem>> suggest(
            @RequestParam String q,
            @RequestParam(defaultValue = "8") int limit) {
        return ResponseEntity.ok(suggestService.suggest(q, limit));
    }

    @GetMapping()
    @SwaggerErrorApi(
            type = {TouristException.class},
            codes = {
                    "INVALID_VISIT_TIME_FORMAT",
                    "UNSUPPORTED_GENDER",
                    "UNSUPPORTED_AGE_GROUP"
            }
    )
    public ResponseEntity<?> filterTouristBySearch(@RequestParam(value = "keyword", required = false) String keyword,
                                                   @ModelAttribute FilterRequestDto filter) {
        List<FilterTouristByDataResponseDto> responseDtoList =
                touristService.filteredTouristBySearch(URLDecoder.decode(keyword, StandardCharsets.UTF_8), filter);

        return ResponseEntity.ok(responseDtoList);
    }
}
