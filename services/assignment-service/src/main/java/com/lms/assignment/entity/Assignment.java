package com.lms.assignment.entity;

import com.lms.common.entity.BaseEntity;
import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.SuperBuilder;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Entity
@Table(name = "assignments")
@Getter @Setter @SuperBuilder @NoArgsConstructor @AllArgsConstructor
public class Assignment extends BaseEntity {
    @Column(name = "title", nullable = false) private String title;
    @Column(name = "description", length = 5000) private String description;
    @Column(name = "course_id", nullable = false) private UUID courseId;
    @Column(name = "module_id") private UUID moduleId;
    @Column(name = "instructor_id", nullable = false) private UUID instructorId;
    @Column(name = "max_score") @Builder.Default private Integer maxScore = 100;
    @Column(name = "passing_score") @Builder.Default private Integer passingScore = 60;
    @Column(name = "due_date") private LocalDateTime dueDate;
    @Column(name = "allow_late_submission") @Builder.Default private boolean allowLateSubmission = false;
    @Column(name = "late_penalty_percent") @Builder.Default private Integer latePenaltyPercent = 10;
    @Column(name = "max_attempts") @Builder.Default private Integer maxAttempts = 1;
    @Column(name = "is_published") @Builder.Default private boolean published = false;
    @OneToMany(mappedBy = "assignment", cascade = CascadeType.ALL)
    @Builder.Default private List<Submission> submissions = new ArrayList<>();
}
