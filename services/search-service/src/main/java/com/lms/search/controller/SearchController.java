package com.lms.search.controller;

import com.lms.common.dto.ApiResponse;
import com.lms.search.document.CourseDocument;
import com.lms.search.document.UserDocument;
import com.lms.search.dto.SearchResult;
import com.lms.search.service.SearchService;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController @RequestMapping("/api/v1/search") @RequiredArgsConstructor
@Tag(name = "Search", description = "Full-text search APIs")
public class SearchController {
    private final SearchService searchService;

    @GetMapping
    public ResponseEntity<ApiResponse<SearchResult>> globalSearch(@RequestParam String q, @PageableDefault(size = 10) Pageable pageable) {
        return ResponseEntity.ok(ApiResponse.success(searchService.globalSearch(q, pageable)));
    }

    @GetMapping("/courses")
    public ResponseEntity<ApiResponse<Page<CourseDocument>>> searchCourses(@RequestParam String q, @PageableDefault(size = 20) Pageable pageable) {
        return ResponseEntity.ok(ApiResponse.success(searchService.searchCourses(q, pageable)));
    }

    @GetMapping("/users")
    public ResponseEntity<ApiResponse<Page<UserDocument>>> searchUsers(@RequestParam String q, @PageableDefault(size = 20) Pageable pageable) {
        return ResponseEntity.ok(ApiResponse.success(searchService.searchUsers(q, pageable)));
    }

    @PostMapping("/courses/index")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<Void>> indexCourse(@RequestBody CourseDocument course) {
        searchService.indexCourse(course);
        return ResponseEntity.ok(ApiResponse.success("Course indexed successfully"));
    }

    @PostMapping("/users/index")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<Void>> indexUser(@RequestBody UserDocument user) {
        searchService.indexUser(user);
        return ResponseEntity.ok(ApiResponse.success("User indexed successfully"));
    }
}
