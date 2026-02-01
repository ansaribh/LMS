package com.lms.content.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UploadRequest {

    @NotBlank(message = "Filename is required")
    private String filename;

    private String mimeType;

    private Long fileSize;

    private UUID courseId;

    private UUID lessonId;

    private String description;
}
