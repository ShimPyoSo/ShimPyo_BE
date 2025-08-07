package com.example.shimpyo.domain.tourist.controller;

import com.example.shimpyo.domain.tourist.dto.*;
import com.example.shimpyo.domain.tourist.service.TouristService;
import com.example.shimpyo.global.SwaggerErrorApi;
import com.example.shimpyo.global.exceptionType.TouristException;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/tourlist")
@Tag(name = "Tourlist", description = "관광지 관련 API 목록" )
public class TouristController {

    private final TouristService touristService;

    @Operation(summary = "관광지 후기 조회")
    @SwaggerErrorApi(type = {TouristException.class}, codes = {"TOURIST_NOT_FOUND"})
    @GetMapping("/reviews")
    public ResponseEntity<List<ReviewResponseDto>> getTouristReview(@RequestParam("touristId") Long touristId,
                                                                    @RequestParam("limit") int limit,
                                                                    @RequestParam(value = "reviewId", required = false) Long reviewId) {
        return ResponseEntity.ok(touristService.getTouristReview(touristId, limit, reviewId));
    }

    @Operation(summary = "후기 작성")
    @PostMapping("/reviews")
    @SwaggerErrorApi(type = {TouristException.class}, codes = {"TOURIST_NOT_FOUND"})
    public ResponseEntity<Void> createReview(@Valid @RequestBody ReviewRequestDto requestDto) {
        touristService.createReview(requestDto);
        return ResponseEntity.ok().build();
    }

    @Operation(summary = "관광지 상세 정보", description = "관광지 id 를 기반으로 관광지 상세정보 출력")
    @SwaggerErrorApi(type = {TouristException.class}, codes = {"TOURIST_NOT_FOUND"})
    @GetMapping("/detail")
    public ResponseEntity<?> detailTourist(@RequestParam("id") Long touristId) {
        TouristDetailResponseDto responseDto = touristService.getTouristDetail(touristId);

        return ResponseEntity.ok(responseDto);
    }

    @GetMapping("/category")
    public ResponseEntity<?> filterTouristByCategory(@RequestParam("category")  String category,
                                                     @ModelAttribute  FilterRequestDto filter,
                                                     @PageableDefault(size = 8) Pageable pageable) {
        List<FilterTouristByCategoryResponseDto> responseDto =
                touristService.filteredTouristByCategory(category, filter, pageable);


        return ResponseEntity.ok(responseDto);
    }

}
