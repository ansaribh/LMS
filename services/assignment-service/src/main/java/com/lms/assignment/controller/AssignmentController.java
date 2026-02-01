package com.lms.assignment.controller;

import com.lms.assignment.entity.Assignment;
import com.lms.assignment.entity.Submission;
import com.lms.assignment.service.AssignmentService;
import com.lms.common.dto.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
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
@RequestMapping("/api/v1/assignments")
@RequiredArgsConstructor
@Tag(name = "Assignments", description = "Assignment management APIs")
public class AssignmentController {
    private final AssignmentService assignmentService;

    @GetMapping("/{id}")
    @Operation(summary = "Get assignment by ID")
    public ResponseEntity<ApiResponse<Assignment>> getById(@PathVariable UUID id) {
        return ResponseEntity.ok(ApiResponse.success(assignmentService.getAssignmentById(id)));
    }

    @GetMapping("/course/{courseId}")
    @Operation(summary = "Get assignments by course")
    public ResponseEntity<ApiResponse<List<Assignment>>> getByCourse(@PathVariable UUID courseId) {
        return ResponseEntity.ok(ApiResponse.success(assignmentService.getAssignmentsByCourse(courseId)));
    }

    @PostMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'INSTRUCTOR')")
    @Operation(summary = "Create assignment")
    public ResponseEntity<ApiResponse<Assignment>> create(@RequestBody Assignment assignment, @AuthenticationPrincipal Jwt jwt) {
        assignment.setInstructorId(UUID.fromString(jwt.getSubject()));
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success(assignmentService.createAssignment(assignment), "Assignment created"));
    }

    @PostMapping("/{id}/submit")
    @Operation(summary = "Submit assignment")
    public ResponseEntity<ApiResponse<Submission>> submit(
            @PathVariable UUID id, @RequestParam(required = false) String content,
            @RequestParam(required = false) String fileUrl, @AuthenticationPrincipal Jwt jwt) {
        UUID studentId = UUID.fromString(jwt.getSubject());
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success(assignmentService.submitAssignment(id, studentId, content, fileUrl), "Submitted"));
    }

    @GetMapping("/{id}/submissions")
    @PreAuthorize("hasAnyRole('ADMIN', 'INSTRUCTOR')")
    @Operation(summary = "Get submissions for assignment")
    public ResponseEntity<ApiResponse<List<Submission>>> getSubmissions(@PathVariable UUID id) {
        return ResponseEntity.ok(ApiResponse.success(assignmentService.getSubmissionsByAssignment(id)));
    }

    @PutMapping("/submissions/{submissionId}/grade")
    @PreAuthorize("hasAnyRole('ADMIN', 'INSTRUCTOR')")
    @Operation(summary = "Grade submission")
    public ResponseEntity<ApiResponse<Submission>> grade(
            @PathVariable UUID submissionId, @RequestParam Integer score,
            @RequestParam(required = false) String feedback, @AuthenticationPrincipal Jwt jwt) {
        UUID gradedBy = UUID.fromString(jwt.getSubject());
        return ResponseEntity.ok(ApiResponse.success(
                assignmentService.gradeSubmission(submissionId, score, feedback, gradedBy), "Graded"));
    }
}
