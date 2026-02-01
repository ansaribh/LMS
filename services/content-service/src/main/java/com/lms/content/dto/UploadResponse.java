package com.lms.content.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UploadResponse {

    private UUID contentId;
    private String uploadUrl;
    private String objectKey;
    private int expiresInMinutes;
}
