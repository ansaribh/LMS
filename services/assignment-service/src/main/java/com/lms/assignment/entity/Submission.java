package com.lms.assignment.entity;

import com.lms.common.entity.BaseEntity;
import com.lms.common.enums.SubmissionStatus;
import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.SuperBuilder;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "submissions")
@Getter @Setter @SuperBuilder @NoArgsConstructor @AllArgsConstructor
public class Submission extends BaseEntity {
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "assignment_id", nullable = false) private Assignment assignment;
    @Column(name = "student_id", nullable = false) private UUID studentId;
    @Column(name = "content", length = 10000) private String content;
    @Column(name = "file_url") private String fileUrl;
    @Column(name = "content_id") private UUID contentId;
    @Enumerated(EnumType.STRING)
    @Column(name = "status") @Builder.Default private SubmissionStatus status = SubmissionStatus.DRAFT;
    @Column(name = "submitted_at") private LocalDateTime submittedAt;
    @Column(name = "score") private Integer score;
    @Column(name = "feedback", length = 5000) private String feedback;
    @Column(name = "graded_by") private UUID gradedBy;
    @Column(name = "graded_at") private LocalDateTime gradedAt;
    @Column(name = "attempt_number") @Builder.Default private Integer attemptNumber = 1;
    @Column(name = "is_late") @Builder.Default private boolean late = false;

    public void submit() {
        this.status = SubmissionStatus.SUBMITTED;
        this.submittedAt = LocalDateTime.now();
    }

    public void grade(Integer score, String feedback, UUID gradedBy) {
        this.score = score;
        this.feedback = feedback;
        this.gradedBy = gradedBy;
        this.gradedAt = LocalDateTime.now();
        this.status = SubmissionStatus.GRADED;
    }
}
