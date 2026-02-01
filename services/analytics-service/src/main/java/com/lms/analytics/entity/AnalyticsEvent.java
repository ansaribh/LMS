package com.lms.analytics.entity;

import com.lms.common.entity.BaseEntity;
import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.SuperBuilder;
import java.util.UUID;

@Entity @Table(name = "analytics_events")
@Getter @Setter @SuperBuilder @NoArgsConstructor @AllArgsConstructor
public class AnalyticsEvent extends BaseEntity {
    @Column(name = "user_id", nullable = false) private UUID userId;
    @Column(name = "course_id") private UUID courseId;
    @Column(name = "lesson_id") private UUID lessonId;
    @Column(name = "action", nullable = false) private String action;
    @Column(name = "resource_type", nullable = false) private String resourceType;
    @Column(name = "duration_seconds") private Long durationSeconds;
    @Column(name = "metadata", length = 2000) private String metadata;
}
