package com.lms.user.service;

import com.lms.common.dto.EnrollmentDto;
import com.lms.common.dto.PagedResponse;
import com.lms.common.enums.EnrollmentStatus;
import com.lms.common.exception.ConflictException;
import com.lms.common.exception.ResourceNotFoundException;
import com.lms.user.dto.EnrollmentRequest;
import com.lms.user.entity.Enrollment;
import com.lms.user.entity.User;
import com.lms.user.mapper.EnrollmentMapper;
import com.lms.user.repository.EnrollmentRepository;
import com.lms.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class EnrollmentService {

    private final EnrollmentRepository enrollmentRepository;
    private final UserRepository userRepository;
    private final EnrollmentMapper enrollmentMapper;

    @Transactional(readOnly = true)
    @Cacheable(value = "enrollments", key = "'user_' + #userId")
    public List<EnrollmentDto> getEnrollmentsByUserId(UUID userId) {
        List<Enrollment> enrollments = enrollmentRepository.findByUserId(userId);
        return enrollments.stream()
                .map(enrollmentMapper::toDto)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public PagedResponse<EnrollmentDto> getEnrollmentsByUserIdPaged(UUID userId, Pageable pageable) {
        Page<Enrollment> enrollments = enrollmentRepository.findByUserId(userId, pageable);
        List<EnrollmentDto> dtos = enrollments.getContent().stream()
                .map(enrollmentMapper::toDto)
                .collect(Collectors.toList());
        return PagedResponse.of(enrollments, dtos);
    }

    @Transactional(readOnly = true)
    public PagedResponse<EnrollmentDto> getEnrollmentsByCourseId(UUID courseId, Pageable pageable) {
        Page<Enrollment> enrollments = enrollmentRepository.findByCourseId(courseId, pageable);
        List<EnrollmentDto> dtos = enrollments.getContent().stream()
                .map(enrollmentMapper::toDto)
                .collect(Collectors.toList());
        return PagedResponse.of(enrollments, dtos);
    }

    @Transactional
    @CacheEvict(value = "enrollments", key = "'user_' + #userId")
    public EnrollmentDto enrollUser(UUID userId, EnrollmentRequest request, UUID enrolledBy) {
        // Check if user exists
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User", "id", userId));

        // Check if already enrolled
        if (enrollmentRepository.existsByUserIdAndCourseId(userId, request.getCourseId())) {
            throw new ConflictException("User is already enrolled in this course");
        }

        Enrollment enrollment = Enrollment.builder()
                .user(user)
                .courseId(request.getCourseId())
                .courseName(request.getCourseName())
                .status(EnrollmentStatus.ACTIVE)
                .progressPercentage(0.0)
                .enrolledAt(LocalDateTime.now())
                .enrolledBy(enrolledBy)
                .build();

        enrollment = enrollmentRepository.save(enrollment);
        log.info("User {} enrolled in course {}", user.getUsername(), request.getCourseId());

        return enrollmentMapper.toDto(enrollment);
    }

    @Transactional
    @CacheEvict(value = "enrollments", key = "'user_' + #userId")
    public EnrollmentDto updateEnrollmentStatus(UUID userId, UUID courseId, EnrollmentStatus status) {
        Enrollment enrollment = enrollmentRepository.findByUserIdAndCourseId(userId, courseId)
                .orElseThrow(() -> new ResourceNotFoundException("Enrollment not found"));

        enrollment.setStatus(status);
        if (status == EnrollmentStatus.COMPLETED) {
            enrollment.markCompleted();
        }

        enrollment = enrollmentRepository.save(enrollment);
        log.info("Enrollment status updated for user {} in course {}: {}", userId, courseId, status);

        return enrollmentMapper.toDto(enrollment);
    }

    @Transactional
    @CacheEvict(value = "enrollments", key = "'user_' + #userId")
    public EnrollmentDto updateProgress(UUID userId, UUID courseId, double progress) {
        Enrollment enrollment = enrollmentRepository.findByUserIdAndCourseId(userId, courseId)
                .orElseThrow(() -> new ResourceNotFoundException("Enrollment not found"));

        enrollment.updateProgress(progress);
        enrollment = enrollmentRepository.save(enrollment);
        
        log.debug("Progress updated for user {} in course {}: {}%", userId, courseId, progress);

        return enrollmentMapper.toDto(enrollment);
    }

    @Transactional
    @CacheEvict(value = "enrollments", key = "'user_' + #userId")
    public void unenrollUser(UUID userId, UUID courseId) {
        Enrollment enrollment = enrollmentRepository.findByUserIdAndCourseId(userId, courseId)
                .orElseThrow(() -> new ResourceNotFoundException("Enrollment not found"));

        enrollment.setStatus(EnrollmentStatus.DROPPED);
        enrollmentRepository.save(enrollment);
        
        log.info("User {} unenrolled from course {}", userId, courseId);
    }

    @Transactional(readOnly = true)
    public boolean isUserEnrolled(UUID userId, UUID courseId) {
        return enrollmentRepository.existsByUserIdAndCourseId(userId, courseId);
    }

    @Transactional(readOnly = true)
    public long getEnrollmentCountByCourse(UUID courseId) {
        return enrollmentRepository.countByCourseIdAndStatus(courseId, EnrollmentStatus.ACTIVE);
    }

    @Transactional(readOnly = true)
    public Double getAverageProgressByCourse(UUID courseId) {
        return enrollmentRepository.getAverageProgressByCourseId(courseId);
    }
}
