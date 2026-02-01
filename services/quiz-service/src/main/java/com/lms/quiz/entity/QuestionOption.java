package com.lms.quiz.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import lombok.*;

@Embeddable
@Getter @Setter @Builder @NoArgsConstructor @AllArgsConstructor
public class QuestionOption {
    @Column(name = "option_text") private String text;
    @Column(name = "is_correct") private boolean correct;
}
