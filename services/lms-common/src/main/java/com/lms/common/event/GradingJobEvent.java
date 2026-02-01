package com.lms.common.event;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

import java.util.UUID;

@Data
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = true)
public class GradingJobEvent extends BaseEvent {
    
    private UUID submissionId;
    private UUID assignmentId;
    private UUID studentId;
    private UUID courseId;
    private String submissionType; // ASSIGNMENT, QUIZ
    private boolean autoGrade;
    private int maxRetries;
    private int currentRetry;

    public static GradingJobEvent create(UUID submissionId, UUID assignmentId, 
                                          UUID studentId, UUID courseId,
                                          String submissionType, boolean autoGrade) {
        GradingJobEvent event = GradingJobEvent.builder()
                .submissionId(submissionId)
                .assignmentId(assignmentId)
                .studentId(studentId)
                .courseId(courseId)
                .submissionType(submissionType)
                .autoGrade(autoGrade)
                .maxRetries(3)
                .currentRetry(0)
                .build();
        event.initializeEvent("GRADING_JOB", "assignment-service");
        return event;
    }
}
