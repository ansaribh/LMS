package com.lms.course.service;

import com.lms.common.dto.LessonDto;
import com.lms.common.exception.ForbiddenException;
import com.lms.common.exception.ResourceNotFoundException;
import com.lms.course.dto.CreateLessonRequest;
import com.lms.course.entity.Course;
import com.lms.course.entity.Lesson;
import com.lms.course.entity.Module;
import com.lms.course.mapper.LessonMapper;
import com.lms.course.repository.CourseRepository;
import com.lms.course.repository.LessonRepository;
import com.lms.course.repository.ModuleRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class LessonService {

    private final LessonRepository lessonRepository;
    private final ModuleRepository moduleRepository;
    private final CourseRepository courseRepository;
    private final LessonMapper lessonMapper;

    @Transactional(readOnly = true)
    public List<LessonDto> getLessonsByModule(UUID moduleId) {
        return lessonRepository.findByModuleIdOrderByOrderIndexAsc(moduleId).stream()
                .map(lessonMapper::toDto)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public LessonDto getLessonById(UUID lessonId) {
        Lesson lesson = lessonRepository.findById(lessonId)
                .orElseThrow(() -> new ResourceNotFoundException("Lesson", "id", lessonId));
        return lessonMapper.toDto(lesson);
    }

    @Transactional
    @CacheEvict(value = "courses", key = "#result.moduleId", condition = "#result != null")
    public LessonDto createLesson(UUID courseId, UUID moduleId, CreateLessonRequest request, 
                                   UUID requesterId, boolean isAdmin) {
        Course course = courseRepository.findById(courseId)
                .orElseThrow(() -> new ResourceNotFoundException("Course", "id", courseId));

        // Check authorization
        if (!isAdmin && !course.getInstructorId().equals(requesterId)) {
            throw new ForbiddenException("You don't have permission to add lessons to this course");
        }

        Module module = moduleRepository.findByCourseIdAndId(courseId, moduleId)
                .orElseThrow(() -> new ResourceNotFoundException("Module", "id", moduleId));

        int orderIndex = request.getOrderIndex() != null 
                ? request.getOrderIndex() 
                : lessonRepository.findMaxOrderIndexByModuleId(moduleId) + 1;

        Lesson lesson = Lesson.builder()
                .module(module)
                .title(request.getTitle())
                .description(request.getDescription())
                .type(request.getType())
                .contentUrl(request.getContentUrl())
                .contentId(request.getContentId())
                .orderIndex(orderIndex)
                .durationMinutes(request.getDurationMinutes())
                .free(request.getFree() != null && request.getFree())
                .transcript(request.getTranscript())
                .additionalResources(request.getAdditionalResources())
                .published(false)
                .build();

        lesson = lessonRepository.save(lesson);
        log.info("Lesson created: {} for module {}", lesson.getTitle(), moduleId);

        return lessonMapper.toDto(lesson);
    }

    @Transactional
    public LessonDto updateLesson(UUID courseId, UUID moduleId, UUID lessonId, CreateLessonRequest request,
                                   UUID requesterId, boolean isAdmin) {
        Course course = courseRepository.findById(courseId)
                .orElseThrow(() -> new ResourceNotFoundException("Course", "id", courseId));

        // Check authorization
        if (!isAdmin && !course.getInstructorId().equals(requesterId)) {
            throw new ForbiddenException("You don't have permission to update lessons in this course");
        }

        Lesson lesson = lessonRepository.findByModuleIdAndId(moduleId, lessonId)
                .orElseThrow(() -> new ResourceNotFoundException("Lesson", "id", lessonId));

        if (request.getTitle() != null) {
            lesson.setTitle(request.getTitle());
        }
        if (request.getDescription() != null) {
            lesson.setDescription(request.getDescription());
        }
        if (request.getType() != null) {
            lesson.setType(request.getType());
        }
        if (request.getContentUrl() != null) {
            lesson.setContentUrl(request.getContentUrl());
        }
        if (request.getContentId() != null) {
            lesson.setContentId(request.getContentId());
        }
        if (request.getOrderIndex() != null) {
            lesson.setOrderIndex(request.getOrderIndex());
        }
        if (request.getDurationMinutes() != null) {
            lesson.setDurationMinutes(request.getDurationMinutes());
        }
        if (request.getFree() != null) {
            lesson.setFree(request.getFree());
        }
        if (request.getTranscript() != null) {
            lesson.setTranscript(request.getTranscript());
        }
        if (request.getAdditionalResources() != null) {
            lesson.setAdditionalResources(request.getAdditionalResources());
        }

        lesson = lessonRepository.save(lesson);
        log.info("Lesson updated: {} in module {}", lesson.getTitle(), moduleId);

        return lessonMapper.toDto(lesson);
    }

    @Transactional
    public void deleteLesson(UUID courseId, UUID moduleId, UUID lessonId, UUID requesterId, boolean isAdmin) {
        Course course = courseRepository.findById(courseId)
                .orElseThrow(() -> new ResourceNotFoundException("Course", "id", courseId));

        // Check authorization
        if (!isAdmin && !course.getInstructorId().equals(requesterId)) {
            throw new ForbiddenException("You don't have permission to delete lessons from this course");
        }

        Lesson lesson = lessonRepository.findByModuleIdAndId(moduleId, lessonId)
                .orElseThrow(() -> new ResourceNotFoundException("Lesson", "id", lessonId));

        lessonRepository.delete(lesson);
        log.info("Lesson deleted: {} from module {}", lessonId, moduleId);
    }

    @Transactional(readOnly = true)
    public List<LessonDto> getFreeLessons(UUID courseId) {
        return lessonRepository.findFreeLessonsByCourseId(courseId).stream()
                .map(lessonMapper::toDto)
                .collect(Collectors.toList());
    }
}
