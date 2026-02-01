package com.lms.user.controller;

import com.lms.common.dto.ApiResponse;
import com.lms.common.dto.PagedResponse;
import com.lms.common.dto.UserDto;
import com.lms.common.enums.UserRole;
import com.lms.user.dto.CreateUserRequest;
import com.lms.user.dto.UpdateUserRequest;
import com.lms.user.service.UserService;
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
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/users")
@RequiredArgsConstructor
@Tag(name = "Users", description = "User management APIs")
public class UserController {

    private final UserService userService;

    @GetMapping
    @Operation(summary = "Get all users", description = "Get paginated list of all active users")
    public ResponseEntity<ApiResponse<PagedResponse<UserDto>>> getAllUsers(
            @PageableDefault(size = 20, sort = "createdAt", direction = Sort.Direction.DESC) Pageable pageable) {
        PagedResponse<UserDto> users = userService.getAllUsers(pageable);
        return ResponseEntity.ok(ApiResponse.success(users));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get user by ID", description = "Get user details by ID")
    public ResponseEntity<ApiResponse<UserDto>> getUserById(@PathVariable UUID id) {
        UserDto user = userService.getUserById(id);
        return ResponseEntity.ok(ApiResponse.success(user));
    }

    @GetMapping("/username/{username}")
    @Operation(summary = "Get user by username", description = "Get user details by username")
    public ResponseEntity<ApiResponse<UserDto>> getUserByUsername(@PathVariable String username) {
        UserDto user = userService.getUserByUsername(username);
        return ResponseEntity.ok(ApiResponse.success(user));
    }

    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Create user", description = "Create a new user (Admin only)")
    public ResponseEntity<ApiResponse<UserDto>> createUser(
            @Valid @RequestBody CreateUserRequest request) {
        UserDto user = userService.createUser(request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success(user, "User created successfully"));
    }

    @PutMapping("/{id}")
    @Operation(summary = "Update user", description = "Update user details")
    public ResponseEntity<ApiResponse<UserDto>> updateUser(
            @PathVariable UUID id,
            @Valid @RequestBody UpdateUserRequest request) {
        UserDto user = userService.updateUser(id, request);
        return ResponseEntity.ok(ApiResponse.success(user, "User updated successfully"));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Delete user", description = "Deactivate user (Admin only)")
    public ResponseEntity<ApiResponse<Void>> deleteUser(@PathVariable UUID id) {
        userService.deleteUser(id);
        return ResponseEntity.ok(ApiResponse.success("User deleted successfully"));
    }

    @GetMapping("/search")
    @Operation(summary = "Search users", description = "Search users by name, email, or username")
    public ResponseEntity<ApiResponse<PagedResponse<UserDto>>> searchUsers(
            @RequestParam String q,
            @PageableDefault(size = 20) Pageable pageable) {
        PagedResponse<UserDto> users = userService.searchUsers(q, pageable);
        return ResponseEntity.ok(ApiResponse.success(users));
    }

    @GetMapping("/role/{role}")
    @Operation(summary = "Get users by role", description = "Get users with specific role")
    public ResponseEntity<ApiResponse<PagedResponse<UserDto>>> getUsersByRole(
            @PathVariable UserRole role,
            @PageableDefault(size = 20) Pageable pageable) {
        PagedResponse<UserDto> users = userService.getUsersByRole(role, pageable);
        return ResponseEntity.ok(ApiResponse.success(users));
    }

    @GetMapping("/instructors")
    @Operation(summary = "Get all instructors", description = "Get all users with instructor role")
    public ResponseEntity<ApiResponse<PagedResponse<UserDto>>> getInstructors(
            @PageableDefault(size = 20) Pageable pageable) {
        PagedResponse<UserDto> users = userService.getUsersByRole(UserRole.INSTRUCTOR, pageable);
        return ResponseEntity.ok(ApiResponse.success(users));
    }

    @GetMapping("/students")
    @Operation(summary = "Get all students", description = "Get all users with student role")
    public ResponseEntity<ApiResponse<PagedResponse<UserDto>>> getStudents(
            @PageableDefault(size = 20) Pageable pageable) {
        PagedResponse<UserDto> users = userService.getUsersByRole(UserRole.STUDENT, pageable);
        return ResponseEntity.ok(ApiResponse.success(users));
    }

    // Role management endpoints
    @PostMapping("/{id}/roles/{role}")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Add role to user", description = "Add a role to user (Admin only)")
    public ResponseEntity<ApiResponse<UserDto>> addRole(
            @PathVariable UUID id,
            @PathVariable UserRole role) {
        UserDto user = userService.addRole(id, role);
        return ResponseEntity.ok(ApiResponse.success(user, "Role added successfully"));
    }

    @DeleteMapping("/{id}/roles/{role}")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Remove role from user", description = "Remove a role from user (Admin only)")
    public ResponseEntity<ApiResponse<UserDto>> removeRole(
            @PathVariable UUID id,
            @PathVariable UserRole role) {
        UserDto user = userService.removeRole(id, role);
        return ResponseEntity.ok(ApiResponse.success(user, "Role removed successfully"));
    }

    // Parent-Student relationship endpoints
    @PostMapping("/{studentId}/parents/{parentId}")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Link parent to student", description = "Create parent-student relationship")
    public ResponseEntity<ApiResponse<Void>> linkParentToStudent(
            @PathVariable UUID studentId,
            @PathVariable UUID parentId) {
        userService.linkParentToStudent(parentId, studentId);
        return ResponseEntity.ok(ApiResponse.success("Parent linked to student successfully"));
    }

    @DeleteMapping("/{studentId}/parents/{parentId}")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Unlink parent from student", description = "Remove parent-student relationship")
    public ResponseEntity<ApiResponse<Void>> unlinkParentFromStudent(
            @PathVariable UUID studentId,
            @PathVariable UUID parentId) {
        userService.unlinkParentFromStudent(parentId, studentId);
        return ResponseEntity.ok(ApiResponse.success("Parent unlinked from student successfully"));
    }

    @GetMapping("/{parentId}/children")
    @Operation(summary = "Get children", description = "Get all children for a parent")
    public ResponseEntity<ApiResponse<List<UserDto>>> getChildren(@PathVariable UUID parentId) {
        List<UserDto> children = userService.getChildrenByParentId(parentId);
        return ResponseEntity.ok(ApiResponse.success(children));
    }

    @GetMapping("/{studentId}/parents")
    @Operation(summary = "Get parents", description = "Get all parents for a student")
    public ResponseEntity<ApiResponse<List<UserDto>>> getParents(@PathVariable UUID studentId) {
        List<UserDto> parents = userService.getParentsByStudentId(studentId);
        return ResponseEntity.ok(ApiResponse.success(parents));
    }
}
