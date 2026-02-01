package com.lms.content.controller;

import com.lms.common.dto.ApiResponse;
import com.lms.content.dto.ContentDto;
import com.lms.content.dto.UploadRequest;
import com.lms.content.dto.UploadResponse;
import com.lms.content.service.ContentService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/content")
@RequiredArgsConstructor
@Tag(name = "Content", description = "Content and media management APIs")
public class ContentController {

    private final ContentService contentService;

    @GetMapping("/{id}")
    @Operation(summary = "Get content by ID", description = "Get content metadata by ID")
    public ResponseEntity<ApiResponse<ContentDto>> getContentById(@PathVariable UUID id) {
        ContentDto content = contentService.getContentById(id);
        return ResponseEntity.ok(ApiResponse.success(content));
    }

    @GetMapping("/course/{courseId}")
    @Operation(summary = "Get content by course", description = "Get all content for a course")
    public ResponseEntity<ApiResponse<List<ContentDto>>> getContentByCourse(@PathVariable UUID courseId) {
        List<ContentDto> content = contentService.getContentByCourse(courseId);
        return ResponseEntity.ok(ApiResponse.success(content));
    }

    @GetMapping("/lesson/{lessonId}")
    @Operation(summary = "Get content by lesson", description = "Get all content for a lesson")
    public ResponseEntity<ApiResponse<List<ContentDto>>> getContentByLesson(@PathVariable UUID lessonId) {
        List<ContentDto> content = contentService.getContentByLesson(lessonId);
        return ResponseEntity.ok(ApiResponse.success(content));
    }

    @PostMapping("/upload-url")
    @PreAuthorize("hasAnyRole('ADMIN', 'INSTRUCTOR')")
    @Operation(summary = "Get presigned upload URL", description = "Generate a presigned URL for direct upload")
    public ResponseEntity<ApiResponse<UploadResponse>> getUploadUrl(
            @Valid @RequestBody UploadRequest request,
            @AuthenticationPrincipal Jwt jwt) {
        UUID uploaderId = UUID.fromString(jwt.getSubject());
        UploadResponse response = contentService.generateUploadUrl(request, uploaderId);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @PostMapping(value = "/upload", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @PreAuthorize("hasAnyRole('ADMIN', 'INSTRUCTOR')")
    @Operation(summary = "Upload file directly", description = "Upload a file directly to the server")
    public ResponseEntity<ApiResponse<ContentDto>> uploadFile(
            @RequestParam("file") MultipartFile file,
            @RequestParam(required = false) UUID courseId,
            @RequestParam(required = false) UUID lessonId,
            @RequestParam(required = false) String description,
            @AuthenticationPrincipal Jwt jwt) {
        UUID uploaderId = UUID.fromString(jwt.getSubject());
        ContentDto content = contentService.uploadFile(file, courseId, lessonId, description, uploaderId);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success(content, "File uploaded successfully"));
    }

    @GetMapping("/{id}/download")
    @Operation(summary = "Get download URL", description = "Get a presigned download URL for content")
    public ResponseEntity<ApiResponse<Map<String, String>>> getDownloadUrl(
            @PathVariable UUID id,
            @AuthenticationPrincipal Jwt jwt) {
        UUID requesterId = UUID.fromString(jwt.getSubject());
        boolean isAdmin = jwt.getClaimAsStringList("roles") != null && 
                jwt.getClaimAsStringList("roles").contains("ADMIN");
        String downloadUrl = contentService.getDownloadUrl(id, requesterId, isAdmin);
        return ResponseEntity.ok(ApiResponse.success(Map.of("downloadUrl", downloadUrl)));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'INSTRUCTOR')")
    @Operation(summary = "Delete content", description = "Delete content from storage")
    public ResponseEntity<ApiResponse<Void>> deleteContent(
            @PathVariable UUID id,
            @AuthenticationPrincipal Jwt jwt) {
        UUID requesterId = UUID.fromString(jwt.getSubject());
        boolean isAdmin = jwt.getClaimAsStringList("roles") != null && 
                jwt.getClaimAsStringList("roles").contains("ADMIN");
        contentService.deleteContent(id, requesterId, isAdmin);
        return ResponseEntity.ok(ApiResponse.success("Content deleted successfully"));
    }

    @PutMapping("/{id}/metadata")
    @PreAuthorize("hasAnyRole('ADMIN', 'INSTRUCTOR')")
    @Operation(summary = "Update content metadata", description = "Update content description and duration")
    public ResponseEntity<ApiResponse<ContentDto>> updateMetadata(
            @PathVariable UUID id,
            @RequestParam(required = false) String description,
            @RequestParam(required = false) Integer durationSeconds,
            @AuthenticationPrincipal Jwt jwt) {
        UUID requesterId = UUID.fromString(jwt.getSubject());
        boolean isAdmin = jwt.getClaimAsStringList("roles") != null && 
                jwt.getClaimAsStringList("roles").contains("ADMIN");
        ContentDto content = contentService.updateContentMetadata(id, description, durationSeconds, requesterId, isAdmin);
        return ResponseEntity.ok(ApiResponse.success(content, "Metadata updated successfully"));
    }
}
