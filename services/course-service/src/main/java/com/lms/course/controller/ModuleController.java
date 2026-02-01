package com.lms.course.controller;

import com.lms.common.dto.ApiResponse;
import com.lms.common.dto.ModuleDto;
import com.lms.course.dto.CreateModuleRequest;
import com.lms.course.service.ModuleService;
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
@RequestMapping("/api/v1/courses/{courseId}/modules")
@RequiredArgsConstructor
@Tag(name = "Modules", description = "Module management APIs")
public class ModuleController {

    private final ModuleService moduleService;

    @GetMapping
    @Operation(summary = "Get modules", description = "Get all modules for a course")
    public ResponseEntity<ApiResponse<List<ModuleDto>>> getModules(@PathVariable UUID courseId) {
        List<ModuleDto> modules = moduleService.getModulesByCourse(courseId);
        return ResponseEntity.ok(ApiResponse.success(modules));
    }

    @GetMapping("/{moduleId}")
    @Operation(summary = "Get module by ID", description = "Get module details with lessons")
    public ResponseEntity<ApiResponse<ModuleDto>> getModuleById(
            @PathVariable UUID courseId,
            @PathVariable UUID moduleId) {
        ModuleDto module = moduleService.getModuleById(moduleId);
        return ResponseEntity.ok(ApiResponse.success(module));
    }

    @PostMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'INSTRUCTOR')")
    @Operation(summary = "Create module", description = "Add a new module to course")
    public ResponseEntity<ApiResponse<ModuleDto>> createModule(
            @PathVariable UUID courseId,
            @Valid @RequestBody CreateModuleRequest request,
            @AuthenticationPrincipal Jwt jwt) {
        UUID requesterId = UUID.fromString(jwt.getSubject());
        boolean isAdmin = jwt.getClaimAsStringList("roles") != null && 
                jwt.getClaimAsStringList("roles").contains("ADMIN");
        ModuleDto module = moduleService.createModule(courseId, request, requesterId, isAdmin);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success(module, "Module created successfully"));
    }

    @PutMapping("/{moduleId}")
    @PreAuthorize("hasAnyRole('ADMIN', 'INSTRUCTOR')")
    @Operation(summary = "Update module", description = "Update module details")
    public ResponseEntity<ApiResponse<ModuleDto>> updateModule(
            @PathVariable UUID courseId,
            @PathVariable UUID moduleId,
            @Valid @RequestBody CreateModuleRequest request,
            @AuthenticationPrincipal Jwt jwt) {
        UUID requesterId = UUID.fromString(jwt.getSubject());
        boolean isAdmin = jwt.getClaimAsStringList("roles") != null && 
                jwt.getClaimAsStringList("roles").contains("ADMIN");
        ModuleDto module = moduleService.updateModule(courseId, moduleId, request, requesterId, isAdmin);
        return ResponseEntity.ok(ApiResponse.success(module, "Module updated successfully"));
    }

    @DeleteMapping("/{moduleId}")
    @PreAuthorize("hasAnyRole('ADMIN', 'INSTRUCTOR')")
    @Operation(summary = "Delete module", description = "Remove module from course")
    public ResponseEntity<ApiResponse<Void>> deleteModule(
            @PathVariable UUID courseId,
            @PathVariable UUID moduleId,
            @AuthenticationPrincipal Jwt jwt) {
        UUID requesterId = UUID.fromString(jwt.getSubject());
        boolean isAdmin = jwt.getClaimAsStringList("roles") != null && 
                jwt.getClaimAsStringList("roles").contains("ADMIN");
        moduleService.deleteModule(courseId, moduleId, requesterId, isAdmin);
        return ResponseEntity.ok(ApiResponse.success("Module deleted successfully"));
    }

    @PutMapping("/reorder")
    @PreAuthorize("hasAnyRole('ADMIN', 'INSTRUCTOR')")
    @Operation(summary = "Reorder modules", description = "Reorder modules in a course")
    public ResponseEntity<ApiResponse<Void>> reorderModules(
            @PathVariable UUID courseId,
            @RequestBody List<UUID> moduleIds,
            @AuthenticationPrincipal Jwt jwt) {
        UUID requesterId = UUID.fromString(jwt.getSubject());
        boolean isAdmin = jwt.getClaimAsStringList("roles") != null && 
                jwt.getClaimAsStringList("roles").contains("ADMIN");
        moduleService.reorderModules(courseId, moduleIds, requesterId, isAdmin);
        return ResponseEntity.ok(ApiResponse.success("Modules reordered successfully"));
    }
}
