package com.lms.course.entity;

import com.lms.common.entity.BaseEntity;
import com.lms.common.enums.CourseStatus;
import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.SuperBuilder;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Entity
@Table(name = "courses")
@Getter
@Setter
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
public class Course extends BaseEntity {

    @Column(name = "title", nullable = false)
    private String title;

    @Column(name = "description", length = 5000)
    private String description;

    @Column(name = "short_description", length = 500)
    private String shortDescription;

    @Column(name = "thumbnail_url")
    private String thumbnailUrl;

    @Column(name = "instructor_id", nullable = false)
    private UUID instructorId;

    @Column(name = "instructor_name")
    private String instructorName;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    @Builder.Default
    private CourseStatus status = CourseStatus.DRAFT;

    @Column(name = "price", precision = 10, scale = 2)
    @Builder.Default
    private BigDecimal price = BigDecimal.ZERO;

    @Column(name = "duration_hours")
    private Integer durationHours;

    @Column(name = "difficulty")
    private String difficulty; // BEGINNER, INTERMEDIATE, ADVANCED

    @ElementCollection
    @CollectionTable(name = "course_tags", joinColumns = @JoinColumn(name = "course_id"))
    @Column(name = "tag")
    @Builder.Default
    private List<String> tags = new ArrayList<>();

    @OneToMany(mappedBy = "course", cascade = CascadeType.ALL, orphanRemoval = true)
    @OrderBy("orderIndex ASC")
    @Builder.Default
    private List<Module> modules = new ArrayList<>();

    @Column(name = "enrollment_count")
    @Builder.Default
    private Integer enrollmentCount = 0;

    @Column(name = "average_rating")
    private Double averageRating;

    @Column(name = "start_date")
    private LocalDateTime startDate;

    @Column(name = "end_date")
    private LocalDateTime endDate;

    @Column(name = "is_featured")
    @Builder.Default
    private boolean featured = false;

    @Column(name = "max_students")
    private Integer maxStudents;

    public void addModule(Module module) {
        modules.add(module);
        module.setCourse(this);
    }

    public void removeModule(Module module) {
        modules.remove(module);
        module.setCourse(null);
    }

    public void incrementEnrollment() {
        this.enrollmentCount = this.enrollmentCount + 1;
    }

    public void decrementEnrollment() {
        if (this.enrollmentCount > 0) {
            this.enrollmentCount = this.enrollmentCount - 1;
        }
    }

    public int getTotalLessons() {
        return modules.stream()
                .mapToInt(m -> m.getLessons().size())
                .sum();
    }

    public int getTotalDurationMinutes() {
        return modules.stream()
                .flatMap(m -> m.getLessons().stream())
                .mapToInt(l -> l.getDurationMinutes() != null ? l.getDurationMinutes() : 0)
                .sum();
    }
}
