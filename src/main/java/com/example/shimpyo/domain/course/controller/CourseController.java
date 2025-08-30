package com.example.shimpyo.domain.course.controller;

import com.example.shimpyo.domain.course.dto.AdditionRecommendsResponseDto;
import com.example.shimpyo.domain.course.service.LikesService;
import com.example.shimpyo.domain.survey.dto.CourseResponseDto;
import com.example.shimpyo.domain.survey.service.SurveyService;
import com.example.shimpyo.global.SwaggerErrorApi;
import com.example.shimpyo.global.exceptionType.CourseException;
import com.example.shimpyo.global.exceptionType.MemberException;
import com.example.shimpyo.global.exceptionType.TouristException;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/course")
@Tag(name = "Course", description = "관광지 관련 API 목록")
public class CourseController {
    private final LikesService likesService;
    private final SurveyService surveyService;

    @Operation(summary = "관광지 찜 추가/삭제")
    @SwaggerErrorApi(type = {TouristException.class, MemberException.class},
            codes = {"TOURIST_NOT_FOUND", "MEMBER_NOT_FOUND"})
    @PatchMapping("/tourist")
    public ResponseEntity<Void> toggleLikeTourist(@RequestBody Map<String, Long> requestDto) {
        likesService.toggleLikeTourist(requestDto.get("id"));
        return ResponseEntity.ok().build();
    }

    @Operation(summary = "관광지 추가 추천 장소")
    @SwaggerErrorApi(type = {MemberException.class, CourseException.class}, codes ={"MEMBER_NOT_FOUND", "COURSE_NOT_FOUND"})
    @GetMapping("/addition")
    public ResponseEntity<List<AdditionRecommendsResponseDto>> additionRecommends(@RequestParam("courseId") Long courseId) {
        return ResponseEntity.ok(surveyService.additionRecommends(courseId));
    }

    /*@Operation(summary = "코스 수정")
    @SwaggerErrorApi(type = {MemberException.class, CourseException.class}, codes ={"MEMBER_NOT_FOUND", "COURSE_NOT_FOUND"})
    @PatchMapping
    public ResponseEntity<Void> modifyCourse(@RequestBody CourseResponseDto requestDto) {
        surveyService.modifyCourse(requestDto);
    }*/

    @Operation(summary = "찜한 코스 삭제")
    @SwaggerErrorApi(type = {MemberException.class, CourseException.class},
            codes = {"MEMBER_NOT_FOUND", "COURSE_NOT_FOUND"})
    @DeleteMapping
    public ResponseEntity<Void> deleteCourse(@RequestParam("id") Long courseId) {
        surveyService.deleteCourse(courseId);
        return ResponseEntity.ok().build();
    }

    @Operation(summary = "공유한 코스 상세 페이지")
    @SwaggerErrorApi(type = {MemberException.class, CourseException.class},
            codes = {"MEMBER_NOT_FOUND", "COURSE_NOT_FOUND"})
    @GetMapping("/share")
    public ResponseEntity<CourseResponseDto> sharedCourse(@RequestParam("courseId") Long courseId,
                                                          @RequestParam("token") String token) {
        return ResponseEntity.ok(surveyService.sharedCourse(courseId, token));
    }
}
