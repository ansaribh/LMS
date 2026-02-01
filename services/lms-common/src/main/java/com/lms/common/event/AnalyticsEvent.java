package com.lms.common.event;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

import java.util.Map;
import java.util.UUID;

@Data
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = true)
public class AnalyticsEvent extends BaseEvent {
    
    private UUID userId;
    private UUID courseId;
    private UUID lessonId;
    private String action; // VIEW, COMPLETE, START, PAUSE, RESUME
    private String resourceType; // COURSE, LESSON, QUIZ, ASSIGNMENT
    private Long durationSeconds;
    private Map<String, Object> metadata;

    public static AnalyticsEvent createCourseView(UUID userId, UUID courseId) {
        AnalyticsEvent event = AnalyticsEvent.builder()
                .userId(userId)
                .courseId(courseId)
                .action("VIEW")
                .resourceType("COURSE")
                .build();
        event.initializeEvent("COURSE_VIEW", "analytics-service");
        return event;
    }

    public static AnalyticsEvent createLessonProgress(UUID userId, UUID courseId, UUID lessonId,
                                                       String action, Long durationSeconds) {
        AnalyticsEvent event = AnalyticsEvent.builder()
                .userId(userId)
                .courseId(courseId)
                .lessonId(lessonId)
                .action(action)
                .resourceType("LESSON")
                .durationSeconds(durationSeconds)
                .build();
        event.initializeEvent("LESSON_PROGRESS", "analytics-service");
        return event;
    }

    public static AnalyticsEvent createQuizCompletion(UUID userId, UUID courseId, UUID quizId,
                                                       Map<String, Object> metadata) {
        AnalyticsEvent event = AnalyticsEvent.builder()
                .userId(userId)
                .courseId(courseId)
                .lessonId(quizId)
                .action("COMPLETE")
                .resourceType("QUIZ")
                .metadata(metadata)
                .build();
        event.initializeEvent("QUIZ_COMPLETION", "analytics-service");
        return event;
    }
}
