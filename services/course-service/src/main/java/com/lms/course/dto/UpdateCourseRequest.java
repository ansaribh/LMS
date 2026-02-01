package com.lms.course.dto;

import com.lms.common.enums.CourseStatus;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UpdateCourseRequest {

    @Size(min = 3, max = 200, message = "Title must be between 3 and 200 characters")
    private String title;

    @Size(max = 5000, message = "Description cannot exceed 5000 characters")
    private String description;

    @Size(max = 500, message = "Short description cannot exceed 500 characters")
    private String shortDescription;

    private String thumbnailUrl;

    private CourseStatus status;

    private BigDecimal price;

    private Integer durationHours;

    private String difficulty;

    private List<String> tags;

    private LocalDateTime startDate;

    private LocalDateTime endDate;

    private Boolean featured;

    private Integer maxStudents;
}
