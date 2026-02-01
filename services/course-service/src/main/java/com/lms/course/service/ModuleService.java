package com.lms.course.service;

import com.lms.common.dto.ModuleDto;
import com.lms.common.exception.ForbiddenException;
import com.lms.common.exception.ResourceNotFoundException;
import com.lms.course.dto.CreateModuleRequest;
import com.lms.course.entity.Course;
import com.lms.course.entity.Module;
import com.lms.course.mapper.ModuleMapper;
import com.lms.course.repository.CourseRepository;
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
public class ModuleService {

    private final ModuleRepository moduleRepository;
    private final CourseRepository courseRepository;
    private final ModuleMapper moduleMapper;

    @Transactional(readOnly = true)
    public List<ModuleDto> getModulesByCourse(UUID courseId) {
        return moduleRepository.findByCourseIdOrderByOrderIndexAsc(courseId).stream()
                .map(moduleMapper::toDto)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public ModuleDto getModuleById(UUID moduleId) {
        Module module = moduleRepository.findByIdWithLessons(moduleId)
                .orElseThrow(() -> new ResourceNotFoundException("Module", "id", moduleId));
        return moduleMapper.toDto(module);
    }

    @Transactional
    @CacheEvict(value = "courses", key = "#courseId")
    public ModuleDto createModule(UUID courseId, CreateModuleRequest request, UUID requesterId, boolean isAdmin) {
        Course course = courseRepository.findById(courseId)
                .orElseThrow(() -> new ResourceNotFoundException("Course", "id", courseId));

        // Check authorization
        if (!isAdmin && !course.getInstructorId().equals(requesterId)) {
            throw new ForbiddenException("You don't have permission to add modules to this course");
        }

        int orderIndex = request.getOrderIndex() != null 
                ? request.getOrderIndex() 
                : moduleRepository.findMaxOrderIndexByCourseId(courseId) + 1;

        Module module = Module.builder()
                .course(course)
                .title(request.getTitle())
                .description(request.getDescription())
                .orderIndex(orderIndex)
                .durationMinutes(request.getDurationMinutes())
                .published(false)
                .build();

        module = moduleRepository.save(module);
        log.info("Module created: {} for course {}", module.getTitle(), courseId);

        return moduleMapper.toDto(module);
    }

    @Transactional
    @CacheEvict(value = "courses", key = "#courseId")
    public ModuleDto updateModule(UUID courseId, UUID moduleId, CreateModuleRequest request, 
                                   UUID requesterId, boolean isAdmin) {
        Course course = courseRepository.findById(courseId)
                .orElseThrow(() -> new ResourceNotFoundException("Course", "id", courseId));

        // Check authorization
        if (!isAdmin && !course.getInstructorId().equals(requesterId)) {
            throw new ForbiddenException("You don't have permission to update modules in this course");
        }

        Module module = moduleRepository.findByCourseIdAndId(courseId, moduleId)
                .orElseThrow(() -> new ResourceNotFoundException("Module", "id", moduleId));

        if (request.getTitle() != null) {
            module.setTitle(request.getTitle());
        }
        if (request.getDescription() != null) {
            module.setDescription(request.getDescription());
        }
        if (request.getOrderIndex() != null) {
            module.setOrderIndex(request.getOrderIndex());
        }
        if (request.getDurationMinutes() != null) {
            module.setDurationMinutes(request.getDurationMinutes());
        }

        module = moduleRepository.save(module);
        log.info("Module updated: {} in course {}", module.getTitle(), courseId);

        return moduleMapper.toDto(module);
    }

    @Transactional
    @CacheEvict(value = "courses", key = "#courseId")
    public void deleteModule(UUID courseId, UUID moduleId, UUID requesterId, boolean isAdmin) {
        Course course = courseRepository.findById(courseId)
                .orElseThrow(() -> new ResourceNotFoundException("Course", "id", courseId));

        // Check authorization
        if (!isAdmin && !course.getInstructorId().equals(requesterId)) {
            throw new ForbiddenException("You don't have permission to delete modules from this course");
        }

        Module module = moduleRepository.findByCourseIdAndId(courseId, moduleId)
                .orElseThrow(() -> new ResourceNotFoundException("Module", "id", moduleId));

        moduleRepository.delete(module);
        log.info("Module deleted: {} from course {}", moduleId, courseId);
    }

    @Transactional
    @CacheEvict(value = "courses", key = "#courseId")
    public void reorderModules(UUID courseId, List<UUID> moduleIds, UUID requesterId, boolean isAdmin) {
        Course course = courseRepository.findById(courseId)
                .orElseThrow(() -> new ResourceNotFoundException("Course", "id", courseId));

        // Check authorization
        if (!isAdmin && !course.getInstructorId().equals(requesterId)) {
            throw new ForbiddenException("You don't have permission to reorder modules in this course");
        }

        for (int i = 0; i < moduleIds.size(); i++) {
            final UUID moduleId = moduleIds.get(i);
            final int orderIndex = i + 1;
            Module module = moduleRepository.findByCourseIdAndId(courseId, moduleId)
                    .orElseThrow(() -> new ResourceNotFoundException("Module", "id", moduleId));
            module.setOrderIndex(orderIndex);
            moduleRepository.save(module);
        }

        log.info("Modules reordered for course {}", courseId);
    }
}
