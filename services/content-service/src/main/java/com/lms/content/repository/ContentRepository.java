package com.lms.content.repository;

import com.lms.common.enums.ContentType;
import com.lms.content.entity.Content;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface ContentRepository extends JpaRepository<Content, UUID> {

    Optional<Content> findByStoredFilename(String storedFilename);

    List<Content> findByCourseId(UUID courseId);

    Page<Content> findByCourseId(UUID courseId, Pageable pageable);

    List<Content> findByLessonId(UUID lessonId);

    List<Content> findByUploadedBy(UUID uploadedBy);

    Page<Content> findByUploadedBy(UUID uploadedBy, Pageable pageable);

    List<Content> findByCourseIdAndContentType(UUID courseId, ContentType contentType);

    long countByCourseId(UUID courseId);

    long countByUploadedBy(UUID uploadedBy);
}
