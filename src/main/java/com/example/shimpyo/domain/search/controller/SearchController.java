package com.example.shimpyo.domain.search.controller;

import com.example.shimpyo.domain.search.service.SearchService;
import com.example.shimpyo.domain.tourist.dto.FilterRequestDto;
import com.example.shimpyo.domain.tourist.dto.FilterTouristByDataResponseDto;
import com.example.shimpyo.domain.tourist.service.TouristService;
import com.example.shimpyo.global.SwaggerErrorApi;
import com.example.shimpyo.global.exceptionType.TouristException;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/search")
@Tag(name = "Search", description = "검색 관련 API 목록")
public class SearchController {

    private final SearchService searchService;
    private final TouristService touristService;

    @Operation(summary = "검색어 자동완성")
    @GetMapping("/autocomplete")
    public ResponseEntity<List<String>> autoComplete(
            @RequestParam("word") String word) {

        List<String> results = searchService.autoComplete(word);
        return ResponseEntity.ok(results);
    }

    /*@PostMapping("/reindex")
    public ResponseEntity<String> reindexTourists() {
        searchService.indexAllTourists();
        return ResponseEntity.ok("Reindexing completed");
    }*/

    @Operation(summary = "검색")
    @GetMapping()
    @SwaggerErrorApi(type = {TouristException.class},
            codes = {"INVALID_VISIT_TIME_FORMAT", "UNSUPPORTED_GENDER", "UNSUPPORTED_AGE_GROUP"})
    public ResponseEntity<?> filterTouristBySearch(@RequestParam(value = "keyword", required = false) String keyword,
                                                   @ModelAttribute FilterRequestDto filter) {
        List<FilterTouristByDataResponseDto> responseDtoList =
                touristService.filteredTourist(filter, null, URLDecoder.decode(keyword, StandardCharsets.UTF_8));
        return ResponseEntity.ok(responseDtoList);
    }
}
