package com.lms.analytics.dto;

import lombok.*;
import java.util.UUID;

@Data @Builder @NoArgsConstructor @AllArgsConstructor
public class CourseAnalytics {
    private UUID courseId;
    private double averageCompletion;
    private long studentsCompleted;
    private long activeStudents;
}
