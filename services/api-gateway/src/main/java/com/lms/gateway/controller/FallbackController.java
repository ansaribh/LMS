package com.lms.gateway.controller;

import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Mono;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

@Slf4j
@RestController
@RequestMapping("/fallback")
public class FallbackController {

    @GetMapping("/auth")
    public Mono<ResponseEntity<Map<String, Object>>> authFallback() {
        log.warn("Auth service fallback triggered");
        return Mono.just(buildFallbackResponse("Authentication service is currently unavailable"));
    }

    @GetMapping("/user")
    public Mono<ResponseEntity<Map<String, Object>>> userFallback() {
        log.warn("User service fallback triggered");
        return Mono.just(buildFallbackResponse("User service is currently unavailable"));
    }

    @GetMapping("/course")
    public Mono<ResponseEntity<Map<String, Object>>> courseFallback() {
        log.warn("Course service fallback triggered");
        return Mono.just(buildFallbackResponse("Course service is currently unavailable"));
    }

    @GetMapping("/content")
    public Mono<ResponseEntity<Map<String, Object>>> contentFallback() {
        log.warn("Content service fallback triggered");
        return Mono.just(buildFallbackResponse("Content service is currently unavailable"));
    }

    @GetMapping("/assignment")
    public Mono<ResponseEntity<Map<String, Object>>> assignmentFallback() {
        log.warn("Assignment service fallback triggered");
        return Mono.just(buildFallbackResponse("Assignment service is currently unavailable"));
    }

    @GetMapping("/quiz")
    public Mono<ResponseEntity<Map<String, Object>>> quizFallback() {
        log.warn("Quiz service fallback triggered");
        return Mono.just(buildFallbackResponse("Quiz service is currently unavailable"));
    }

    @GetMapping("/attendance")
    public Mono<ResponseEntity<Map<String, Object>>> attendanceFallback() {
        log.warn("Attendance service fallback triggered");
        return Mono.just(buildFallbackResponse("Attendance service is currently unavailable"));
    }

    @GetMapping("/messaging")
    public Mono<ResponseEntity<Map<String, Object>>> messagingFallback() {
        log.warn("Messaging service fallback triggered");
        return Mono.just(buildFallbackResponse("Messaging service is currently unavailable"));
    }

    @GetMapping("/analytics")
    public Mono<ResponseEntity<Map<String, Object>>> analyticsFallback() {
        log.warn("Analytics service fallback triggered");
        return Mono.just(buildFallbackResponse("Analytics service is currently unavailable"));
    }

    @GetMapping("/search")
    public Mono<ResponseEntity<Map<String, Object>>> searchFallback() {
        log.warn("Search service fallback triggered");
        return Mono.just(buildFallbackResponse("Search service is currently unavailable"));
    }

    private ResponseEntity<Map<String, Object>> buildFallbackResponse(String message) {
        Map<String, Object> response = new HashMap<>();
        response.put("success", false);
        response.put("message", message);
        response.put("error", Map.of(
                "code", "SERVICE_UNAVAILABLE",
                "details", "The requested service is temporarily unavailable. Please try again later."
        ));
        response.put("timestamp", LocalDateTime.now().toString());

        return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE).body(response);
    }
}
