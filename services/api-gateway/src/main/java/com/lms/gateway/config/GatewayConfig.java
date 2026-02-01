package com.lms.gateway.config;

import org.springframework.cloud.gateway.route.RouteLocator;
import org.springframework.cloud.gateway.route.builder.RouteLocatorBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class GatewayConfig {

    @Bean
    public RouteLocator customRouteLocator(RouteLocatorBuilder builder) {
        return builder.routes()
                // Auth Service Routes
                .route("auth-service", r -> r
                        .path("/api/v1/auth/**")
                        .filters(f -> f
                                .stripPrefix(0)
                                .circuitBreaker(c -> c
                                        .setName("authServiceCircuitBreaker")
                                        .setFallbackUri("forward:/fallback/auth")))
                        .uri("lb://auth-service"))

                // User Service Routes
                .route("user-service", r -> r
                        .path("/api/v1/users/**")
                        .filters(f -> f
                                .stripPrefix(0)
                                .circuitBreaker(c -> c
                                        .setName("userServiceCircuitBreaker")
                                        .setFallbackUri("forward:/fallback/user")))
                        .uri("lb://user-service"))

                // Course Service Routes
                .route("course-service", r -> r
                        .path("/api/v1/courses/**", "/api/v1/modules/**", "/api/v1/lessons/**")
                        .filters(f -> f
                                .stripPrefix(0)
                                .circuitBreaker(c -> c
                                        .setName("courseServiceCircuitBreaker")
                                        .setFallbackUri("forward:/fallback/course")))
                        .uri("lb://course-service"))

                // Content Service Routes
                .route("content-service", r -> r
                        .path("/api/v1/content/**")
                        .filters(f -> f
                                .stripPrefix(0)
                                .circuitBreaker(c -> c
                                        .setName("contentServiceCircuitBreaker")
                                        .setFallbackUri("forward:/fallback/content")))
                        .uri("lb://content-service"))

                // Assignment Service Routes
                .route("assignment-service", r -> r
                        .path("/api/v1/assignments/**", "/api/v1/submissions/**")
                        .filters(f -> f
                                .stripPrefix(0)
                                .circuitBreaker(c -> c
                                        .setName("assignmentServiceCircuitBreaker")
                                        .setFallbackUri("forward:/fallback/assignment")))
                        .uri("lb://assignment-service"))

                // Quiz Service Routes
                .route("quiz-service", r -> r
                        .path("/api/v1/quizzes/**", "/api/v1/exams/**")
                        .filters(f -> f
                                .stripPrefix(0)
                                .circuitBreaker(c -> c
                                        .setName("quizServiceCircuitBreaker")
                                        .setFallbackUri("forward:/fallback/quiz")))
                        .uri("lb://quiz-service"))

                // Attendance Service Routes
                .route("attendance-service", r -> r
                        .path("/api/v1/attendance/**")
                        .filters(f -> f
                                .stripPrefix(0)
                                .circuitBreaker(c -> c
                                        .setName("attendanceServiceCircuitBreaker")
                                        .setFallbackUri("forward:/fallback/attendance")))
                        .uri("lb://attendance-service"))

                // Messaging Service Routes
                .route("messaging-service", r -> r
                        .path("/api/v1/notifications/**", "/api/v1/messages/**")
                        .filters(f -> f
                                .stripPrefix(0)
                                .circuitBreaker(c -> c
                                        .setName("messagingServiceCircuitBreaker")
                                        .setFallbackUri("forward:/fallback/messaging")))
                        .uri("lb://messaging-service"))

                // Analytics Service Routes
                .route("analytics-service", r -> r
                        .path("/api/v1/analytics/**", "/api/v1/reports/**")
                        .filters(f -> f
                                .stripPrefix(0)
                                .circuitBreaker(c -> c
                                        .setName("analyticsServiceCircuitBreaker")
                                        .setFallbackUri("forward:/fallback/analytics")))
                        .uri("lb://analytics-service"))

                // Search Service Routes
                .route("search-service", r -> r
                        .path("/api/v1/search/**")
                        .filters(f -> f
                                .stripPrefix(0)
                                .circuitBreaker(c -> c
                                        .setName("searchServiceCircuitBreaker")
                                        .setFallbackUri("forward:/fallback/search")))
                        .uri("lb://search-service"))

                .build();
    }
}
