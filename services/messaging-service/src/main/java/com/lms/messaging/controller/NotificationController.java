package com.lms.messaging.controller;

import com.lms.common.dto.ApiResponse;
import com.lms.common.enums.NotificationType;
import com.lms.messaging.entity.Notification;
import com.lms.messaging.service.NotificationService;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.*;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@RestController @RequestMapping("/api/v1/notifications") @RequiredArgsConstructor
@Tag(name = "Notifications", description = "Notification APIs")
public class NotificationController {
    private final NotificationService notificationService;

    @GetMapping
    public ResponseEntity<ApiResponse<List<Notification>>> getMyNotifications(@AuthenticationPrincipal Jwt jwt) {
        return ResponseEntity.ok(ApiResponse.success(notificationService.getUserNotifications(UUID.fromString(jwt.getSubject()))));
    }

    @GetMapping("/user/{userId}")
    public ResponseEntity<ApiResponse<List<Notification>>> getUserNotifications(@PathVariable UUID userId) {
        return ResponseEntity.ok(ApiResponse.success(notificationService.getUserNotifications(userId)));
    }

    @PutMapping("/{id}/read")
    public ResponseEntity<ApiResponse<Notification>> markAsRead(@PathVariable UUID id) {
        return ResponseEntity.ok(ApiResponse.success(notificationService.markAsRead(id)));
    }

    @GetMapping("/unread-count")
    public ResponseEntity<ApiResponse<Map<String, Long>>> getUnreadCount(@AuthenticationPrincipal Jwt jwt) {
        return ResponseEntity.ok(ApiResponse.success(Map.of("count", notificationService.getUnreadCount(UUID.fromString(jwt.getSubject())))));
    }

    @PostMapping("/send")
    public ResponseEntity<ApiResponse<Notification>> send(
            @RequestParam UUID recipientId, @RequestParam(required = false) String email,
            @RequestParam NotificationType type, @RequestParam String subject, @RequestParam String body) {
        return ResponseEntity.ok(ApiResponse.success(notificationService.sendNotification(recipientId, email, type, subject, body)));
    }
}
