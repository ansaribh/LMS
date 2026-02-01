package com.lms.analytics.repository;

import com.lms.analytics.entity.StudentProgress;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface StudentProgressRepository extends JpaRepository<StudentProgress, UUID> {
    Optional<StudentProgress> findByStudentIdAndCourseId(UUID studentId, UUID courseId);
    List<StudentProgress> findByStudentId(UUID studentId);
    List<StudentProgress> findByCourseId(UUID courseId);
    @Query("SELECT AVG(p.completionPercentage) FROM StudentProgress p WHERE p.courseId = :courseId")
    Double getAverageCompletionByCourse(@Param("courseId") UUID courseId);
    @Query("SELECT COUNT(p) FROM StudentProgress p WHERE p.courseId = :courseId AND p.completionPercentage = 100")
    long countCompletedByCourse(@Param("courseId") UUID courseId);
}
