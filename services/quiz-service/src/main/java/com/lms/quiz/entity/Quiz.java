package com.lms.quiz.entity;

import com.lms.common.entity.BaseEntity;
import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.SuperBuilder;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Entity @Table(name = "quizzes")
@Getter @Setter @SuperBuilder @NoArgsConstructor @AllArgsConstructor
public class Quiz extends BaseEntity {
    @Column(name = "title", nullable = false) private String title;
    @Column(name = "description", length = 2000) private String description;
    @Column(name = "course_id", nullable = false) private UUID courseId;
    @Column(name = "module_id") private UUID moduleId;
    @Column(name = "instructor_id", nullable = false) private UUID instructorId;
    @Column(name = "time_limit_minutes") private Integer timeLimitMinutes;
    @Column(name = "passing_score") @Builder.Default private Integer passingScore = 60;
    @Column(name = "max_attempts") @Builder.Default private Integer maxAttempts = 3;
    @Column(name = "shuffle_questions") @Builder.Default private boolean shuffleQuestions = false;
    @Column(name = "show_answers_after") @Builder.Default private boolean showAnswersAfter = true;
    @Column(name = "start_date") private LocalDateTime startDate;
    @Column(name = "end_date") private LocalDateTime endDate;
    @Column(name = "is_published") @Builder.Default private boolean published = false;
    @OneToMany(mappedBy = "quiz", cascade = CascadeType.ALL, orphanRemoval = true)
    @OrderBy("orderIndex ASC")
    @Builder.Default private List<Question> questions = new ArrayList<>();
}
