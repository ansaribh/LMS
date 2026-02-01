package com.lms.content.service;

import com.lms.common.enums.ContentType;
import com.lms.common.exception.ForbiddenException;
import com.lms.common.exception.ResourceNotFoundException;
import com.lms.content.config.MinioConfig;
import com.lms.content.dto.ContentDto;
import com.lms.content.dto.UploadRequest;
import com.lms.content.dto.UploadResponse;
import com.lms.content.entity.Content;
import com.lms.content.mapper.ContentMapper;
import com.lms.content.repository.ContentRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class ContentService {

    private final ContentRepository contentRepository;
    private final MinioService minioService;
    private final MinioConfig minioConfig;
    private final ContentMapper contentMapper;

    @Transactional(readOnly = true)
    public ContentDto getContentById(UUID id) {
        Content content = contentRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Content", "id", id));
        return contentMapper.toDto(content);
    }

    @Transactional(readOnly = true)
    public List<ContentDto> getContentByCourse(UUID courseId) {
        return contentRepository.findByCourseId(courseId).stream()
                .map(contentMapper::toDto)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<ContentDto> getContentByLesson(UUID lessonId) {
        return contentRepository.findByLessonId(lessonId).stream()
                .map(contentMapper::toDto)
                .collect(Collectors.toList());
    }

    @Transactional
    public UploadResponse generateUploadUrl(UploadRequest request, UUID uploaderId) {
        String storedFilename = UUID.randomUUID().toString() + "_" + sanitizeFilename(request.getFilename());
        String objectKey = buildObjectKey(request.getCourseId(), storedFilename);
        
        String uploadUrl = minioService.getPresignedUploadUrl(
                minioConfig.getBucketName(),
                objectKey,
                15 // 15 minutes expiry
        );

        // Create content record (pending upload)
        Content content = Content.builder()
                .originalFilename(request.getFilename())
                .storedFilename(storedFilename)
                .contentType(determineContentType(request.getFilename()))
                .mimeType(request.getMimeType())
                .fileSize(request.getFileSize())
                .bucketName(minioConfig.getBucketName())
                .objectKey(objectKey)
                .courseId(request.getCourseId())
                .lessonId(request.getLessonId())
                .uploadedBy(uploaderId)
                .description(request.getDescription())
                .isPublic(false)
                .build();

        content = contentRepository.save(content);
        log.info("Upload URL generated for content: {}", content.getId());

        return UploadResponse.builder()
                .contentId(content.getId())
                .uploadUrl(uploadUrl)
                .objectKey(objectKey)
                .expiresInMinutes(15)
                .build();
    }

    @Transactional
    public ContentDto uploadFile(MultipartFile file, UUID courseId, UUID lessonId, 
                                  String description, UUID uploaderId) {
        String storedFilename = UUID.randomUUID().toString() + "_" + sanitizeFilename(file.getOriginalFilename());
        String objectKey = buildObjectKey(courseId, storedFilename);

        // Upload to MinIO
        minioService.uploadFile(minioConfig.getBucketName(), objectKey, file);

        // Create content record
        Content content = Content.builder()
                .originalFilename(file.getOriginalFilename())
                .storedFilename(storedFilename)
                .contentType(determineContentType(file.getOriginalFilename()))
                .mimeType(file.getContentType())
                .fileSize(file.getSize())
                .bucketName(minioConfig.getBucketName())
                .objectKey(objectKey)
                .courseId(courseId)
                .lessonId(lessonId)
                .uploadedBy(uploaderId)
                .description(description)
                .isPublic(false)
                .build();

        content = contentRepository.save(content);
        log.info("File uploaded: {} by user {}", content.getId(), uploaderId);

        return contentMapper.toDto(content);
    }

    @Transactional
    public String getDownloadUrl(UUID contentId, UUID requesterId, boolean isAdmin) {
        Content content = contentRepository.findById(contentId)
                .orElseThrow(() -> new ResourceNotFoundException("Content", "id", contentId));

        // Update view count
        content.incrementViewCount();
        contentRepository.save(content);

        return minioService.getPresignedDownloadUrl(
                content.getBucketName(),
                content.getObjectKey(),
                60 // 1 hour expiry
        );
    }

    @Transactional
    public void deleteContent(UUID contentId, UUID requesterId, boolean isAdmin) {
        Content content = contentRepository.findById(contentId)
                .orElseThrow(() -> new ResourceNotFoundException("Content", "id", contentId));

        // Check authorization
        if (!isAdmin && !content.getUploadedBy().equals(requesterId)) {
            throw new ForbiddenException("You don't have permission to delete this content");
        }

        // Delete from MinIO
        minioService.deleteFile(content.getBucketName(), content.getObjectKey());

        // Delete from database
        contentRepository.delete(content);
        log.info("Content deleted: {} by user {}", contentId, requesterId);
    }

    @Transactional
    public ContentDto updateContentMetadata(UUID contentId, String description, 
                                             Integer durationSeconds, UUID requesterId, boolean isAdmin) {
        Content content = contentRepository.findById(contentId)
                .orElseThrow(() -> new ResourceNotFoundException("Content", "id", contentId));

        // Check authorization
        if (!isAdmin && !content.getUploadedBy().equals(requesterId)) {
            throw new ForbiddenException("You don't have permission to update this content");
        }

        if (description != null) {
            content.setDescription(description);
        }
        if (durationSeconds != null) {
            content.setDurationSeconds(durationSeconds);
        }

        content = contentRepository.save(content);
        return contentMapper.toDto(content);
    }

    private String buildObjectKey(UUID courseId, String filename) {
        if (courseId != null) {
            return "courses/" + courseId + "/" + filename;
        }
        return "uploads/" + filename;
    }

    private String sanitizeFilename(String filename) {
        return filename.replaceAll("[^a-zA-Z0-9.-]", "_");
    }

    private ContentType determineContentType(String filename) {
        String extension = filename.substring(filename.lastIndexOf('.') + 1).toLowerCase();
        return switch (extension) {
            case "mp4", "avi", "mov", "mkv", "webm" -> ContentType.VIDEO;
            case "pdf" -> ContentType.PDF;
            case "jpg", "jpeg", "png", "gif", "webp" -> ContentType.IMAGE;
            case "doc", "docx", "ppt", "pptx", "xls", "xlsx" -> ContentType.DOCUMENT;
            case "mp3", "wav", "ogg" -> ContentType.AUDIO;
            case "zip", "rar", "7z" -> ContentType.ARCHIVE;
            default -> ContentType.DOCUMENT;
        };
    }
}
