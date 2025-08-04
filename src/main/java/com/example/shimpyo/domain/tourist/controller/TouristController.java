package com.example.shimpyo.domain.tourist.controller;

import com.example.shimpyo.domain.tourist.dto.ReviewResponseDto;
import com.example.shimpyo.domain.tourist.dto.ReviewRequestDto;
import com.example.shimpyo.domain.tourist.dto.TouristDetailResponseDto;
import com.example.shimpyo.domain.tourist.service.TouristService;
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
public class TouristController {

    private final TouristService touristService;

    @GetMapping("/reviews")
    public ResponseEntity<List<ReviewResponseDto>> getTouristReview(@RequestParam("touristId") Long touristId,
                                                                    @RequestParam("limit") int limit,
                                                                    @RequestParam(value = "reviewId", required = false) Long reviewId) {
        return ResponseEntity.ok(touristService.getTouristReview(touristId, limit, reviewId));
    }

    @PostMapping("/reviews")
    public ResponseEntity<Void> createReview(@Valid @RequestBody ReviewRequestDto requestDto) {
        touristService.createReview(requestDto);
        return ResponseEntity.ok().build();
    }

    @GetMapping("/detail")
    public ResponseEntity<?> detailTourist(@RequestParam("id") Long touristId) {
        TouristDetailResponseDto responseDto = touristService.getTouristDetail(touristId);

        return ResponseEntity.ok(responseDto);
    }

}
