package com.lms.quiz.controller;

import com.lms.common.dto.ApiResponse;
import com.lms.quiz.entity.Quiz;
import com.lms.quiz.entity.QuizAttempt;
import com.lms.quiz.service.QuizService;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.*;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@RestController @RequestMapping("/api/v1/quizzes") @RequiredArgsConstructor
@Tag(name = "Quizzes", description = "Quiz management APIs")
public class QuizController {
    private final QuizService quizService;

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<Quiz>> getById(@PathVariable UUID id) {
        return ResponseEntity.ok(ApiResponse.success(quizService.getQuizById(id)));
    }

    @GetMapping("/course/{courseId}")
    public ResponseEntity<ApiResponse<List<Quiz>>> getByCourse(@PathVariable UUID courseId) {
        return ResponseEntity.ok(ApiResponse.success(quizService.getQuizzesByCourse(courseId)));
    }

    @PostMapping @PreAuthorize("hasAnyRole('ADMIN', 'INSTRUCTOR')")
    public ResponseEntity<ApiResponse<Quiz>> create(@RequestBody Quiz quiz, @AuthenticationPrincipal Jwt jwt) {
        quiz.setInstructorId(UUID.fromString(jwt.getSubject()));
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.success(quizService.createQuiz(quiz)));
    }

    @PostMapping("/{id}/start")
    public ResponseEntity<ApiResponse<QuizAttempt>> startAttempt(@PathVariable UUID id, @AuthenticationPrincipal Jwt jwt) {
        return ResponseEntity.ok(ApiResponse.success(quizService.startAttempt(id, UUID.fromString(jwt.getSubject()))));
    }

    @PostMapping("/attempts/{attemptId}/submit")
    public ResponseEntity<ApiResponse<QuizAttempt>> submit(@PathVariable UUID attemptId, @RequestBody Map<UUID, String> answers) {
        return ResponseEntity.ok(ApiResponse.success(quizService.submitAttempt(attemptId, answers)));
    }

    @GetMapping("/{id}/attempts")
    public ResponseEntity<ApiResponse<List<QuizAttempt>>> getAttempts(@PathVariable UUID id, @AuthenticationPrincipal Jwt jwt) {
        return ResponseEntity.ok(ApiResponse.success(quizService.getStudentAttempts(id, UUID.fromString(jwt.getSubject()))));
    }
}
