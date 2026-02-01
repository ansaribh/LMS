package com.lms.common.dto;

import com.lms.common.enums.CourseStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CourseDto {
    
    private UUID id;
    private String title;
    private String description;
    private String shortDescription;
    private String thumbnailUrl;
    private UUID instructorId;
    private String instructorName;
    private CourseStatus status;
    private BigDecimal price;
    private Integer durationHours;
    private String difficulty;
    private List<String> tags;
    private List<ModuleDto> modules;
    private Integer enrollmentCount;
    private Double averageRating;
    private LocalDateTime startDate;
    private LocalDateTime endDate;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
