package com.lms.course.service;

import com.lms.common.dto.CourseDto;
import com.lms.common.dto.PagedResponse;
import com.lms.common.enums.CourseStatus;
import com.lms.common.exception.ForbiddenException;
import com.lms.common.exception.ResourceNotFoundException;
import com.lms.course.dto.CreateCourseRequest;
import com.lms.course.dto.UpdateCourseRequest;
import com.lms.course.entity.Course;
import com.lms.course.mapper.CourseMapper;
import com.lms.course.repository.CourseRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class CourseService {

    private final CourseRepository courseRepository;
    private final CourseMapper courseMapper;

    @Transactional(readOnly = true)
    public PagedResponse<CourseDto> getAllPublishedCourses(Pageable pageable) {
        Page<Course> courses = courseRepository.findByStatus(CourseStatus.PUBLISHED, pageable);
        List<CourseDto> dtos = courses.getContent().stream()
                .map(courseMapper::toDto)
                .collect(Collectors.toList());
        return PagedResponse.of(courses, dtos);
    }

    @Transactional(readOnly = true)
    @Cacheable(value = "courses", key = "#id")
    public CourseDto getCourseById(UUID id) {
        Course course = courseRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Course", "id", id));
        return courseMapper.toDto(course);
    }

    @Transactional(readOnly = true)
    public PagedResponse<CourseDto> getCoursesByInstructor(UUID instructorId, Pageable pageable) {
        Page<Course> courses = courseRepository.findByInstructorId(instructorId, pageable);
        List<CourseDto> dtos = courses.getContent().stream()
                .map(courseMapper::toDto)
                .collect(Collectors.toList());
        return PagedResponse.of(courses, dtos);
    }

    @Transactional
    public CourseDto createCourse(CreateCourseRequest request, UUID instructorId) {
        Course course = Course.builder()
                .title(request.getTitle())
                .description(request.getDescription())
                .shortDescription(request.getShortDescription())
                .thumbnailUrl(request.getThumbnailUrl())
                .instructorId(instructorId)
                .instructorName(request.getInstructorName())
                .status(CourseStatus.DRAFT)
                .price(request.getPrice())
                .durationHours(request.getDurationHours())
                .difficulty(request.getDifficulty())
                .tags(request.getTags())
                .startDate(request.getStartDate())
                .endDate(request.getEndDate())
                .maxStudents(request.getMaxStudents())
                .build();

        course = courseRepository.save(course);
        log.info("Course created: {} by instructor {}", course.getTitle(), instructorId);

        return courseMapper.toDto(course);
    }

    @Transactional
    @CacheEvict(value = "courses", key = "#id")
    public CourseDto updateCourse(UUID id, UpdateCourseRequest request, UUID requesterId, boolean isAdmin) {
        Course course = courseRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Course", "id", id));

        // Check authorization
        if (!isAdmin && !course.getInstructorId().equals(requesterId)) {
            throw new ForbiddenException("You don't have permission to update this course");
        }

        if (request.getTitle() != null) {
            course.setTitle(request.getTitle());
        }
        if (request.getDescription() != null) {
            course.setDescription(request.getDescription());
        }
        if (request.getShortDescription() != null) {
            course.setShortDescription(request.getShortDescription());
        }
        if (request.getThumbnailUrl() != null) {
            course.setThumbnailUrl(request.getThumbnailUrl());
        }
        if (request.getStatus() != null) {
            course.setStatus(request.getStatus());
        }
        if (request.getPrice() != null) {
            course.setPrice(request.getPrice());
        }
        if (request.getDurationHours() != null) {
            course.setDurationHours(request.getDurationHours());
        }
        if (request.getDifficulty() != null) {
            course.setDifficulty(request.getDifficulty());
        }
        if (request.getTags() != null) {
            course.setTags(request.getTags());
        }
        if (request.getStartDate() != null) {
            course.setStartDate(request.getStartDate());
        }
        if (request.getEndDate() != null) {
            course.setEndDate(request.getEndDate());
        }
        if (request.getFeatured() != null) {
            course.setFeatured(request.getFeatured());
        }
        if (request.getMaxStudents() != null) {
            course.setMaxStudents(request.getMaxStudents());
        }

        course = courseRepository.save(course);
        log.info("Course updated: {}", course.getTitle());

        return courseMapper.toDto(course);
    }

    @Transactional
    @CacheEvict(value = "courses", key = "#id")
    public void deleteCourse(UUID id, UUID requesterId, boolean isAdmin) {
        Course course = courseRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Course", "id", id));

        // Check authorization
        if (!isAdmin && !course.getInstructorId().equals(requesterId)) {
            throw new ForbiddenException("You don't have permission to delete this course");
        }

        // Soft delete by changing status
        course.setStatus(CourseStatus.ARCHIVED);
        courseRepository.save(course);
        log.info("Course archived: {}", course.getTitle());
    }

    @Transactional
    @CacheEvict(value = "courses", key = "#id")
    public CourseDto publishCourse(UUID id, UUID requesterId, boolean isAdmin) {
        Course course = courseRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Course", "id", id));

        // Check authorization
        if (!isAdmin && !course.getInstructorId().equals(requesterId)) {
            throw new ForbiddenException("You don't have permission to publish this course");
        }

        course.setStatus(CourseStatus.PUBLISHED);
        course = courseRepository.save(course);
        log.info("Course published: {}", course.getTitle());

        return courseMapper.toDto(course);
    }

    @Transactional(readOnly = true)
    public PagedResponse<CourseDto> searchCourses(String query, Pageable pageable) {
        Page<Course> courses = courseRepository.searchPublishedCourses(query, pageable);
        List<CourseDto> dtos = courses.getContent().stream()
                .map(courseMapper::toDto)
                .collect(Collectors.toList());
        return PagedResponse.of(courses, dtos);
    }

    @Transactional(readOnly = true)
    public List<CourseDto> getPopularCourses(int limit) {
        return courseRepository.findPopularCourses(PageRequest.of(0, limit)).stream()
                .map(courseMapper::toDto)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<CourseDto> getFeaturedCourses() {
        return courseRepository.findFeaturedCourses().stream()
                .map(courseMapper::toDto)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public PagedResponse<CourseDto> getCoursesByTag(String tag, Pageable pageable) {
        Page<Course> courses = courseRepository.findByTag(tag, pageable);
        List<CourseDto> dtos = courses.getContent().stream()
                .map(courseMapper::toDto)
                .collect(Collectors.toList());
        return PagedResponse.of(courses, dtos);
    }

    @Transactional(readOnly = true)
    public List<String> getAllTags() {
        return courseRepository.findAllTags();
    }

    @Transactional
    @CacheEvict(value = "courses", key = "#courseId")
    public void incrementEnrollment(UUID courseId) {
        Course course = courseRepository.findById(courseId)
                .orElseThrow(() -> new ResourceNotFoundException("Course", "id", courseId));
        course.incrementEnrollment();
        courseRepository.save(course);
    }

    @Transactional
    @CacheEvict(value = "courses", key = "#courseId")
    public void decrementEnrollment(UUID courseId) {
        Course course = courseRepository.findById(courseId)
                .orElseThrow(() -> new ResourceNotFoundException("Course", "id", courseId));
        course.decrementEnrollment();
        courseRepository.save(course);
    }
}
