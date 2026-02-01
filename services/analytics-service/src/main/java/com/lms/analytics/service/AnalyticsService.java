package com.lms.analytics.service;

import com.lms.analytics.dto.CourseAnalytics;
import com.lms.analytics.dto.StudentAnalytics;
import com.lms.analytics.entity.StudentProgress;
import com.lms.analytics.repository.AnalyticsEventRepository;
import com.lms.analytics.repository.StudentProgressRepository;
import com.lms.common.event.AnalyticsEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Slf4j @Service @RequiredArgsConstructor
public class AnalyticsService {
    private final StudentProgressRepository progressRepository;
    private final AnalyticsEventRepository eventRepository;

    @KafkaListener(topics = "analytics-events", groupId = "analytics-service")
    public void handleAnalyticsEvent(AnalyticsEvent event) {
        log.info("Received analytics event: {} for user {}", event.getAction(), event.getUserId());
        com.lms.analytics.entity.AnalyticsEvent entity = com.lms.analytics.entity.AnalyticsEvent.builder()
                .userId(event.getUserId()).courseId(event.getCourseId()).lessonId(event.getLessonId())
                .action(event.getAction()).resourceType(event.getResourceType()).durationSeconds(event.getDurationSeconds()).build();
        eventRepository.save(entity);
        if (event.getCourseId() != null && "COMPLETE".equals(event.getAction())) {
            updateStudentProgress(event.getUserId(), event.getCourseId());
        }
    }

    @Transactional
    public void updateStudentProgress(UUID studentId, UUID courseId) {
        StudentProgress progress = progressRepository.findByStudentIdAndCourseId(studentId, courseId)
                .orElse(StudentProgress.builder().studentId(studentId).courseId(courseId).build());
        progress.setLastActivityAt(LocalDateTime.now());
        Long timeSpent = eventRepository.getTotalTimeSpent(studentId, courseId);
        if (timeSpent != null) progress.setTotalTimeSpentMinutes(timeSpent / 60);
        progress.updateCompletionPercentage();
        progressRepository.save(progress);
    }

    @Transactional(readOnly = true)
    public StudentAnalytics getStudentAnalytics(UUID studentId) {
        List<StudentProgress> progresses = progressRepository.findByStudentId(studentId);
        double avgCompletion = progresses.stream().mapToDouble(StudentProgress::getCompletionPercentage).average().orElse(0);
        long totalTime = progresses.stream().mapToLong(StudentProgress::getTotalTimeSpentMinutes).sum();
        return StudentAnalytics.builder().studentId(studentId).coursesEnrolled(progresses.size())
                .averageCompletion(avgCompletion).totalTimeSpentMinutes(totalTime).build();
    }

    @Transactional(readOnly = true)
    public CourseAnalytics getCourseAnalytics(UUID courseId) {
        Double avgCompletion = progressRepository.getAverageCompletionByCourse(courseId);
        long completed = progressRepository.countCompletedByCourse(courseId);
        long activeUsers = eventRepository.countActiveUsersByCourse(courseId);
        return CourseAnalytics.builder().courseId(courseId).averageCompletion(avgCompletion != null ? avgCompletion : 0)
                .studentsCompleted(completed).activeStudents(activeUsers).build();
    }

    @Transactional(readOnly = true)
    public StudentProgress getStudentCourseProgress(UUID studentId, UUID courseId) {
        return progressRepository.findByStudentIdAndCourseId(studentId, courseId).orElse(null);
    }
}
