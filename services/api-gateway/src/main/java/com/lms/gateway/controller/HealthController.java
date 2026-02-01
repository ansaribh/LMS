package com.lms.gateway.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Mono;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/health")
public class HealthController {

    @GetMapping
    public Mono<ResponseEntity<Map<String, Object>>> health() {
        Map<String, Object> response = new HashMap<>();
        response.put("status", "UP");
        response.put("service", "api-gateway");
        response.put("timestamp", LocalDateTime.now().toString());
        
        return Mono.just(ResponseEntity.ok(response));
    }

    @GetMapping("/ready")
    public Mono<ResponseEntity<Map<String, Object>>> readiness() {
        Map<String, Object> response = new HashMap<>();
        response.put("status", "READY");
        response.put("service", "api-gateway");
        response.put("timestamp", LocalDateTime.now().toString());
        
        return Mono.just(ResponseEntity.ok(response));
    }

    @GetMapping("/live")
    public Mono<ResponseEntity<Map<String, Object>>> liveness() {
        Map<String, Object> response = new HashMap<>();
        response.put("status", "LIVE");
        response.put("service", "api-gateway");
        response.put("timestamp", LocalDateTime.now().toString());
        
        return Mono.just(ResponseEntity.ok(response));
    }
}
