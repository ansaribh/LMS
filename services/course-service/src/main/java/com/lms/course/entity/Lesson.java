package com.lms.course.entity;

import com.lms.common.entity.BaseEntity;
import com.lms.common.enums.LessonType;
import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.SuperBuilder;

@Entity
@Table(name = "lessons")
@Getter
@Setter
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
public class Lesson extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "module_id", nullable = false)
    private Module module;

    @Column(name = "title", nullable = false)
    private String title;

    @Column(name = "description", length = 2000)
    private String description;

    @Enumerated(EnumType.STRING)
    @Column(name = "type", nullable = false)
    private LessonType type;

    @Column(name = "content_url")
    private String contentUrl;

    @Column(name = "content_id")
    private String contentId;

    @Column(name = "order_index", nullable = false)
    private Integer orderIndex;

    @Column(name = "duration_minutes")
    private Integer durationMinutes;

    @Column(name = "is_free")
    @Builder.Default
    private boolean free = false;

    @Column(name = "is_published")
    @Builder.Default
    private boolean published = false;

    @Column(name = "transcript", length = 10000)
    private String transcript;

    @Column(name = "additional_resources", length = 2000)
    private String additionalResources;
}
