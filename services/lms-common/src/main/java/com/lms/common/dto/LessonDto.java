package com.lms.common.dto;

import com.lms.common.enums.LessonType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class LessonDto {
    
    private UUID id;
    private UUID moduleId;
    private String title;
    private String description;
    private LessonType type;
    private String contentUrl;
    private Integer orderIndex;
    private Integer durationMinutes;
    private boolean isFree;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
