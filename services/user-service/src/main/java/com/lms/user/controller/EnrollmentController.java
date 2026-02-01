package com.lms.user.controller;

import com.lms.common.dto.ApiResponse;
import com.lms.common.dto.EnrollmentDto;
import com.lms.common.dto.PagedResponse;
import com.lms.common.enums.EnrollmentStatus;
import com.lms.user.dto.EnrollmentRequest;
import com.lms.user.service.EnrollmentService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/users")
@RequiredArgsConstructor
@Tag(name = "Enrollments", description = "Enrollment management APIs")
public class EnrollmentController {

    private final EnrollmentService enrollmentService;

    @GetMapping("/{userId}/enrollments")
    @Operation(summary = "Get user enrollments", description = "Get all enrollments for a user")
    public ResponseEntity<ApiResponse<List<EnrollmentDto>>> getUserEnrollments(
            @PathVariable UUID userId) {
        List<EnrollmentDto> enrollments = enrollmentService.getEnrollmentsByUserId(userId);
        return ResponseEntity.ok(ApiResponse.success(enrollments));
    }

    @GetMapping("/{userId}/enrollments/paged")
    @Operation(summary = "Get user enrollments (paged)", description = "Get paginated enrollments for a user")
    public ResponseEntity<ApiResponse<PagedResponse<EnrollmentDto>>> getUserEnrollmentsPaged(
            @PathVariable UUID userId,
            @PageableDefault(size = 20) Pageable pageable) {
        PagedResponse<EnrollmentDto> enrollments = enrollmentService.getEnrollmentsByUserIdPaged(userId, pageable);
        return ResponseEntity.ok(ApiResponse.success(enrollments));
    }

    @PostMapping("/{userId}/enrollments")
    @Operation(summary = "Enroll user in course", description = "Enroll a user in a course")
    public ResponseEntity<ApiResponse<EnrollmentDto>> enrollUser(
            @PathVariable UUID userId,
            @Valid @RequestBody EnrollmentRequest request,
            @AuthenticationPrincipal Jwt jwt) {
        UUID enrolledBy = UUID.fromString(jwt.getSubject());
        EnrollmentDto enrollment = enrollmentService.enrollUser(userId, request, enrolledBy);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success(enrollment, "User enrolled successfully"));
    }

    @PutMapping("/{userId}/enrollments/{courseId}/status")
    @Operation(summary = "Update enrollment status", description = "Update the status of an enrollment")
    public ResponseEntity<ApiResponse<EnrollmentDto>> updateEnrollmentStatus(
            @PathVariable UUID userId,
            @PathVariable UUID courseId,
            @RequestParam EnrollmentStatus status) {
        EnrollmentDto enrollment = enrollmentService.updateEnrollmentStatus(userId, courseId, status);
        return ResponseEntity.ok(ApiResponse.success(enrollment, "Enrollment status updated"));
    }

    @PutMapping("/{userId}/enrollments/{courseId}/progress")
    @Operation(summary = "Update enrollment progress", description = "Update the progress of an enrollment")
    public ResponseEntity<ApiResponse<EnrollmentDto>> updateProgress(
            @PathVariable UUID userId,
            @PathVariable UUID courseId,
            @RequestParam double progress) {
        EnrollmentDto enrollment = enrollmentService.updateProgress(userId, courseId, progress);
        return ResponseEntity.ok(ApiResponse.success(enrollment, "Progress updated"));
    }

    @DeleteMapping("/{userId}/enrollments/{courseId}")
    @Operation(summary = "Unenroll user from course", description = "Remove user from a course")
    public ResponseEntity<ApiResponse<Void>> unenrollUser(
            @PathVariable UUID userId,
            @PathVariable UUID courseId) {
        enrollmentService.unenrollUser(userId, courseId);
        return ResponseEntity.ok(ApiResponse.success("User unenrolled successfully"));
    }

    @GetMapping("/{userId}/enrollments/{courseId}/check")
    @Operation(summary = "Check enrollment", description = "Check if user is enrolled in a course")
    public ResponseEntity<ApiResponse<Boolean>> checkEnrollment(
            @PathVariable UUID userId,
            @PathVariable UUID courseId) {
        boolean enrolled = enrollmentService.isUserEnrolled(userId, courseId);
        return ResponseEntity.ok(ApiResponse.success(enrolled));
    }
}
