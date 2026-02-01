package com.lms.quiz.entity;

import com.lms.common.entity.BaseEntity;
import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.SuperBuilder;

@Entity @Table(name = "quiz_answers")
@Getter @Setter @SuperBuilder @NoArgsConstructor @AllArgsConstructor
public class QuizAnswer extends BaseEntity {
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "attempt_id", nullable = false) private QuizAttempt attempt;
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "question_id", nullable = false) private Question question;
    @Column(name = "answer", length = 2000) private String answer;
    @Column(name = "is_correct") private boolean correct;
}
