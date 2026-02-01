package com.lms.course.controller;

import com.lms.common.dto.ApiResponse;
import com.lms.common.dto.LessonDto;
import com.lms.course.dto.CreateLessonRequest;
import com.lms.course.service.LessonService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/courses/{courseId}/modules/{moduleId}/lessons")
@RequiredArgsConstructor
@Tag(name = "Lessons", description = "Lesson management APIs")
public class LessonController {

    private final LessonService lessonService;

    @GetMapping
    @Operation(summary = "Get lessons", description = "Get all lessons for a module")
    public ResponseEntity<ApiResponse<List<LessonDto>>> getLessons(
            @PathVariable UUID courseId,
            @PathVariable UUID moduleId) {
        List<LessonDto> lessons = lessonService.getLessonsByModule(moduleId);
        return ResponseEntity.ok(ApiResponse.success(lessons));
    }

    @GetMapping("/{lessonId}")
    @Operation(summary = "Get lesson by ID", description = "Get lesson details")
    public ResponseEntity<ApiResponse<LessonDto>> getLessonById(
            @PathVariable UUID courseId,
            @PathVariable UUID moduleId,
            @PathVariable UUID lessonId) {
        LessonDto lesson = lessonService.getLessonById(lessonId);
        return ResponseEntity.ok(ApiResponse.success(lesson));
    }

    @PostMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'INSTRUCTOR')")
    @Operation(summary = "Create lesson", description = "Add a new lesson to module")
    public ResponseEntity<ApiResponse<LessonDto>> createLesson(
            @PathVariable UUID courseId,
            @PathVariable UUID moduleId,
            @Valid @RequestBody CreateLessonRequest request,
            @AuthenticationPrincipal Jwt jwt) {
        UUID requesterId = UUID.fromString(jwt.getSubject());
        boolean isAdmin = jwt.getClaimAsStringList("roles") != null && 
                jwt.getClaimAsStringList("roles").contains("ADMIN");
        LessonDto lesson = lessonService.createLesson(courseId, moduleId, request, requesterId, isAdmin);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success(lesson, "Lesson created successfully"));
    }

    @PutMapping("/{lessonId}")
    @PreAuthorize("hasAnyRole('ADMIN', 'INSTRUCTOR')")
    @Operation(summary = "Update lesson", description = "Update lesson details")
    public ResponseEntity<ApiResponse<LessonDto>> updateLesson(
            @PathVariable UUID courseId,
            @PathVariable UUID moduleId,
            @PathVariable UUID lessonId,
            @Valid @RequestBody CreateLessonRequest request,
            @AuthenticationPrincipal Jwt jwt) {
        UUID requesterId = UUID.fromString(jwt.getSubject());
        boolean isAdmin = jwt.getClaimAsStringList("roles") != null && 
                jwt.getClaimAsStringList("roles").contains("ADMIN");
        LessonDto lesson = lessonService.updateLesson(courseId, moduleId, lessonId, request, requesterId, isAdmin);
        return ResponseEntity.ok(ApiResponse.success(lesson, "Lesson updated successfully"));
    }

    @DeleteMapping("/{lessonId}")
    @PreAuthorize("hasAnyRole('ADMIN', 'INSTRUCTOR')")
    @Operation(summary = "Delete lesson", description = "Remove lesson from module")
    public ResponseEntity<ApiResponse<Void>> deleteLesson(
            @PathVariable UUID courseId,
            @PathVariable UUID moduleId,
            @PathVariable UUID lessonId,
            @AuthenticationPrincipal Jwt jwt) {
        UUID requesterId = UUID.fromString(jwt.getSubject());
        boolean isAdmin = jwt.getClaimAsStringList("roles") != null && 
                jwt.getClaimAsStringList("roles").contains("ADMIN");
        lessonService.deleteLesson(courseId, moduleId, lessonId, requesterId, isAdmin);
        return ResponseEntity.ok(ApiResponse.success("Lesson deleted successfully"));
    }
}
