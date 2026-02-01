package com.lms.analytics.controller;

import com.lms.analytics.dto.CourseAnalytics;
import com.lms.analytics.dto.StudentAnalytics;
import com.lms.analytics.entity.StudentProgress;
import com.lms.analytics.service.AnalyticsService;
import com.lms.common.dto.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.*;
import java.util.UUID;

@RestController @RequestMapping("/api/v1/analytics") @RequiredArgsConstructor
@Tag(name = "Analytics", description = "Analytics APIs")
public class AnalyticsController {
    private final AnalyticsService analyticsService;

    @GetMapping("/student/{studentId}/progress")
    public ResponseEntity<ApiResponse<StudentAnalytics>> getStudentProgress(@PathVariable UUID studentId) {
        return ResponseEntity.ok(ApiResponse.success(analyticsService.getStudentAnalytics(studentId)));
    }

    @GetMapping("/student/me/progress")
    public ResponseEntity<ApiResponse<StudentAnalytics>> getMyProgress(@AuthenticationPrincipal Jwt jwt) {
        return ResponseEntity.ok(ApiResponse.success(analyticsService.getStudentAnalytics(UUID.fromString(jwt.getSubject()))));
    }

    @GetMapping("/course/{courseId}/completion")
    public ResponseEntity<ApiResponse<CourseAnalytics>> getCourseCompletion(@PathVariable UUID courseId) {
        return ResponseEntity.ok(ApiResponse.success(analyticsService.getCourseAnalytics(courseId)));
    }

    @GetMapping("/student/{studentId}/course/{courseId}")
    public ResponseEntity<ApiResponse<StudentProgress>> getStudentCourseProgress(@PathVariable UUID studentId, @PathVariable UUID courseId) {
        return ResponseEntity.ok(ApiResponse.success(analyticsService.getStudentCourseProgress(studentId, courseId)));
    }
}
