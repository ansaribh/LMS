package com.lms.content.dto;

import com.lms.common.enums.ContentType;
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
public class ContentDto {

    private UUID id;
    private String originalFilename;
    private ContentType contentType;
    private String mimeType;
    private Long fileSize;
    private UUID courseId;
    private UUID lessonId;
    private UUID uploadedBy;
    private String description;
    private Integer durationSeconds;
    private String thumbnailUrl;
    private Long viewCount;
    private Long downloadCount;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
