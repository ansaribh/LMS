package com.lms.attendance.controller;

import com.lms.attendance.entity.Attendance;
import com.lms.attendance.service.AttendanceService;
import com.lms.common.dto.ApiResponse;
import com.lms.common.enums.AttendanceStatus;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.*;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@RestController @RequestMapping("/api/v1/attendance") @RequiredArgsConstructor
@Tag(name = "Attendance", description = "Attendance management APIs")
public class AttendanceController {
    private final AttendanceService attendanceService;

    @PostMapping("/mark") @PreAuthorize("hasAnyRole('ADMIN', 'INSTRUCTOR')")
    public ResponseEntity<ApiResponse<Attendance>> mark(
            @RequestParam UUID courseId, @RequestParam UUID studentId,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date,
            @RequestParam AttendanceStatus status, @RequestParam(required = false) String remarks,
            @AuthenticationPrincipal Jwt jwt) {
        return ResponseEntity.ok(ApiResponse.success(
                attendanceService.markAttendance(courseId, studentId, date, status, UUID.fromString(jwt.getSubject()), remarks)));
    }

    @GetMapping("/course/{courseId}/date/{date}")
    public ResponseEntity<ApiResponse<List<Attendance>>> getByDate(
            @PathVariable UUID courseId, @PathVariable @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date) {
        return ResponseEntity.ok(ApiResponse.success(attendanceService.getAttendanceByDate(courseId, date)));
    }

    @GetMapping("/student/{studentId}")
    public ResponseEntity<ApiResponse<List<Attendance>>> getByStudent(@PathVariable UUID studentId) {
        return ResponseEntity.ok(ApiResponse.success(attendanceService.getStudentAttendance(studentId)));
    }

    @GetMapping("/student/{studentId}/course/{courseId}/percentage")
    public ResponseEntity<ApiResponse<Map<String, Double>>> getPercentage(@PathVariable UUID studentId, @PathVariable UUID courseId) {
        return ResponseEntity.ok(ApiResponse.success(Map.of("percentage", attendanceService.getAttendancePercentage(studentId, courseId))));
    }
}
