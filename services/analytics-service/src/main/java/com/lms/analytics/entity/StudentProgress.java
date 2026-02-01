package com.lms.analytics.entity;

import com.lms.common.entity.BaseEntity;
import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.SuperBuilder;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity @Table(name = "student_progress", uniqueConstraints = @UniqueConstraint(columnNames = {"student_id", "course_id"}))
@Getter @Setter @SuperBuilder @NoArgsConstructor @AllArgsConstructor
public class StudentProgress extends BaseEntity {
    @Column(name = "student_id", nullable = false) private UUID studentId;
    @Column(name = "course_id", nullable = false) private UUID courseId;
    @Column(name = "lessons_completed") @Builder.Default private Integer lessonsCompleted = 0;
    @Column(name = "total_lessons") @Builder.Default private Integer totalLessons = 0;
    @Column(name = "quizzes_completed") @Builder.Default private Integer quizzesCompleted = 0;
    @Column(name = "assignments_completed") @Builder.Default private Integer assignmentsCompleted = 0;
    @Column(name = "average_quiz_score") private Double averageQuizScore;
    @Column(name = "average_assignment_score") private Double averageAssignmentScore;
    @Column(name = "total_time_spent_minutes") @Builder.Default private Long totalTimeSpentMinutes = 0L;
    @Column(name = "last_activity_at") private LocalDateTime lastActivityAt;
    @Column(name = "completion_percentage") @Builder.Default private Double completionPercentage = 0.0;

    public void updateCompletionPercentage() {
        if (totalLessons > 0) this.completionPercentage = (lessonsCompleted * 100.0) / totalLessons;
    }
}
