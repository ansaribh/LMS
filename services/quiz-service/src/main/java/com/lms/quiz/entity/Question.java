package com.lms.quiz.entity;

import com.lms.common.entity.BaseEntity;
import com.lms.common.enums.QuestionType;
import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.SuperBuilder;
import java.util.ArrayList;
import java.util.List;

@Entity @Table(name = "questions")
@Getter @Setter @SuperBuilder @NoArgsConstructor @AllArgsConstructor
public class Question extends BaseEntity {
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "quiz_id", nullable = false) private Quiz quiz;
    @Column(name = "text", nullable = false, length = 2000) private String text;
    @Enumerated(EnumType.STRING)
    @Column(name = "type", nullable = false) private QuestionType type;
    @Column(name = "points") @Builder.Default private Integer points = 1;
    @Column(name = "order_index") private Integer orderIndex;
    @Column(name = "explanation", length = 2000) private String explanation;
    @ElementCollection @CollectionTable(name = "question_options", joinColumns = @JoinColumn(name = "question_id"))
    @Builder.Default private List<QuestionOption> options = new ArrayList<>();
    @Column(name = "correct_answer") private String correctAnswer;
}
