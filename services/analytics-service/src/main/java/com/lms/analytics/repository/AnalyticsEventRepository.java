package com.lms.analytics.repository;

import com.lms.analytics.entity.AnalyticsEvent;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.util.UUID;

@Repository
public interface AnalyticsEventRepository extends JpaRepository<AnalyticsEvent, UUID> {
    Page<AnalyticsEvent> findByUserId(UUID userId, Pageable pageable);
    Page<AnalyticsEvent> findByCourseId(UUID courseId, Pageable pageable);
    @Query("SELECT SUM(e.durationSeconds) FROM AnalyticsEvent e WHERE e.userId = :userId AND e.courseId = :courseId")
    Long getTotalTimeSpent(@Param("userId") UUID userId, @Param("courseId") UUID courseId);
    @Query("SELECT COUNT(DISTINCT e.userId) FROM AnalyticsEvent e WHERE e.courseId = :courseId")
    long countActiveUsersByCourse(@Param("courseId") UUID courseId);
}
