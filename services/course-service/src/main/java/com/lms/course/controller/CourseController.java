package com.lms.course.controller;

import com.lms.common.dto.ApiResponse;
import com.lms.common.dto.CourseDto;
import com.lms.common.dto.PagedResponse;
import com.lms.course.dto.CreateCourseRequest;
import com.lms.course.dto.UpdateCourseRequest;
import com.lms.course.service.CourseService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/courses")
@RequiredArgsConstructor
@Tag(name = "Courses", description = "Course management APIs")
public class CourseController {

    private final CourseService courseService;

    @GetMapping
    @Operation(summary = "Get all published courses", description = "Get paginated list of published courses")
    public ResponseEntity<ApiResponse<PagedResponse<CourseDto>>> getAllCourses(
            @PageableDefault(size = 20, sort = "createdAt", direction = Sort.Direction.DESC) Pageable pageable) {
        PagedResponse<CourseDto> courses = courseService.getAllPublishedCourses(pageable);
        return ResponseEntity.ok(ApiResponse.success(courses));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get course by ID", description = "Get course details by ID")
    public ResponseEntity<ApiResponse<CourseDto>> getCourseById(@PathVariable UUID id) {
        CourseDto course = courseService.getCourseById(id);
        return ResponseEntity.ok(ApiResponse.success(course));
    }

    @GetMapping("/instructor/{instructorId}")
    @Operation(summary = "Get courses by instructor", description = "Get all courses for an instructor")
    public ResponseEntity<ApiResponse<PagedResponse<CourseDto>>> getCoursesByInstructor(
            @PathVariable UUID instructorId,
            @PageableDefault(size = 20) Pageable pageable) {
        PagedResponse<CourseDto> courses = courseService.getCoursesByInstructor(instructorId, pageable);
        return ResponseEntity.ok(ApiResponse.success(courses));
    }

    @PostMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'INSTRUCTOR')")
    @Operation(summary = "Create course", description = "Create a new course")
    public ResponseEntity<ApiResponse<CourseDto>> createCourse(
            @Valid @RequestBody CreateCourseRequest request,
            @AuthenticationPrincipal Jwt jwt) {
        UUID instructorId = UUID.fromString(jwt.getSubject());
        CourseDto course = courseService.createCourse(request, instructorId);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success(course, "Course created successfully"));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'INSTRUCTOR')")
    @Operation(summary = "Update course", description = "Update course details")
    public ResponseEntity<ApiResponse<CourseDto>> updateCourse(
            @PathVariable UUID id,
            @Valid @RequestBody UpdateCourseRequest request,
            @AuthenticationPrincipal Jwt jwt) {
        UUID requesterId = UUID.fromString(jwt.getSubject());
        boolean isAdmin = jwt.getClaimAsStringList("roles") != null && 
                jwt.getClaimAsStringList("roles").contains("ADMIN");
        CourseDto course = courseService.updateCourse(id, request, requesterId, isAdmin);
        return ResponseEntity.ok(ApiResponse.success(course, "Course updated successfully"));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'INSTRUCTOR')")
    @Operation(summary = "Delete course", description = "Archive a course")
    public ResponseEntity<ApiResponse<Void>> deleteCourse(
            @PathVariable UUID id,
            @AuthenticationPrincipal Jwt jwt) {
        UUID requesterId = UUID.fromString(jwt.getSubject());
        boolean isAdmin = jwt.getClaimAsStringList("roles") != null && 
                jwt.getClaimAsStringList("roles").contains("ADMIN");
        courseService.deleteCourse(id, requesterId, isAdmin);
        return ResponseEntity.ok(ApiResponse.success("Course deleted successfully"));
    }

    @PostMapping("/{id}/publish")
    @PreAuthorize("hasAnyRole('ADMIN', 'INSTRUCTOR')")
    @Operation(summary = "Publish course", description = "Publish a draft course")
    public ResponseEntity<ApiResponse<CourseDto>> publishCourse(
            @PathVariable UUID id,
            @AuthenticationPrincipal Jwt jwt) {
        UUID requesterId = UUID.fromString(jwt.getSubject());
        boolean isAdmin = jwt.getClaimAsStringList("roles") != null && 
                jwt.getClaimAsStringList("roles").contains("ADMIN");
        CourseDto course = courseService.publishCourse(id, requesterId, isAdmin);
        return ResponseEntity.ok(ApiResponse.success(course, "Course published successfully"));
    }

    @GetMapping("/search")
    @Operation(summary = "Search courses", description = "Search published courses by title or description")
    public ResponseEntity<ApiResponse<PagedResponse<CourseDto>>> searchCourses(
            @RequestParam String q,
            @PageableDefault(size = 20) Pageable pageable) {
        PagedResponse<CourseDto> courses = courseService.searchCourses(q, pageable);
        return ResponseEntity.ok(ApiResponse.success(courses));
    }

    @GetMapping("/popular")
    @Operation(summary = "Get popular courses", description = "Get most popular courses by enrollment")
    public ResponseEntity<ApiResponse<List<CourseDto>>> getPopularCourses(
            @RequestParam(defaultValue = "10") int limit) {
        List<CourseDto> courses = courseService.getPopularCourses(limit);
        return ResponseEntity.ok(ApiResponse.success(courses));
    }

    @GetMapping("/featured")
    @Operation(summary = "Get featured courses", description = "Get featured courses")
    public ResponseEntity<ApiResponse<List<CourseDto>>> getFeaturedCourses() {
        List<CourseDto> courses = courseService.getFeaturedCourses();
        return ResponseEntity.ok(ApiResponse.success(courses));
    }

    @GetMapping("/tag/{tag}")
    @Operation(summary = "Get courses by tag", description = "Get courses with specific tag")
    public ResponseEntity<ApiResponse<PagedResponse<CourseDto>>> getCoursesByTag(
            @PathVariable String tag,
            @PageableDefault(size = 20) Pageable pageable) {
        PagedResponse<CourseDto> courses = courseService.getCoursesByTag(tag, pageable);
        return ResponseEntity.ok(ApiResponse.success(courses));
    }

    @GetMapping("/tags")
    @Operation(summary = "Get all tags", description = "Get all unique course tags")
    public ResponseEntity<ApiResponse<List<String>>> getAllTags() {
        List<String> tags = courseService.getAllTags();
        return ResponseEntity.ok(ApiResponse.success(tags));
    }
}
