package com.lms.analytics.dto;

import lombok.*;
import java.util.UUID;

@Data @Builder @NoArgsConstructor @AllArgsConstructor
public class StudentAnalytics {
    private UUID studentId;
    private int coursesEnrolled;
    private double averageCompletion;
    private long totalTimeSpentMinutes;
}
