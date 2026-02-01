package com.lms.user.repository;

import com.lms.common.enums.EnrollmentStatus;
import com.lms.user.entity.Enrollment;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface EnrollmentRepository extends JpaRepository<Enrollment, UUID> {

    Optional<Enrollment> findByUserIdAndCourseId(UUID userId, UUID courseId);

    List<Enrollment> findByUserId(UUID userId);

    Page<Enrollment> findByUserId(UUID userId, Pageable pageable);

    List<Enrollment> findByCourseId(UUID courseId);

    Page<Enrollment> findByCourseId(UUID courseId, Pageable pageable);

    List<Enrollment> findByUserIdAndStatus(UUID userId, EnrollmentStatus status);

    boolean existsByUserIdAndCourseId(UUID userId, UUID courseId);

    @Query("SELECT COUNT(e) FROM Enrollment e WHERE e.courseId = :courseId AND e.status = :status")
    long countByCourseIdAndStatus(@Param("courseId") UUID courseId, @Param("status") EnrollmentStatus status);

    @Query("SELECT e FROM Enrollment e WHERE e.user.id = :userId ORDER BY e.lastAccessedAt DESC NULLS LAST")
    List<Enrollment> findRecentEnrollmentsByUserId(@Param("userId") UUID userId, Pageable pageable);

    @Query("SELECT AVG(e.progressPercentage) FROM Enrollment e WHERE e.courseId = :courseId")
    Double getAverageProgressByCourseId(@Param("courseId") UUID courseId);
}
