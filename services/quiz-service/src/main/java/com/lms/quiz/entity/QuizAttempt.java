package com.lms.quiz.entity;

import com.lms.common.entity.BaseEntity;
import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.SuperBuilder;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Entity @Table(name = "quiz_attempts")
@Getter @Setter @SuperBuilder @NoArgsConstructor @AllArgsConstructor
public class QuizAttempt extends BaseEntity {
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "quiz_id", nullable = false) private Quiz quiz;
    @Column(name = "student_id", nullable = false) private UUID studentId;
    @Column(name = "started_at", nullable = false) private LocalDateTime startedAt;
    @Column(name = "submitted_at") private LocalDateTime submittedAt;
    @Column(name = "score") private Integer score;
    @Column(name = "max_score") private Integer maxScore;
    @Column(name = "percentage") private Double percentage;
    @Column(name = "passed") private Boolean passed;
    @Column(name = "attempt_number") @Builder.Default private Integer attemptNumber = 1;
    @OneToMany(mappedBy = "attempt", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default private List<QuizAnswer> answers = new ArrayList<>();

    public void calculateScore(int passingScore) {
        this.score = answers.stream().filter(QuizAnswer::isCorrect).mapToInt(a -> a.getQuestion().getPoints()).sum();
        this.maxScore = answers.stream().mapToInt(a -> a.getQuestion().getPoints()).sum();
        this.percentage = maxScore > 0 ? (score * 100.0 / maxScore) : 0;
        this.passed = percentage >= passingScore;
    }
}
