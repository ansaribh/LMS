package com.lms.course.repository;

import com.lms.common.enums.CourseStatus;
import com.lms.course.entity.Course;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface CourseRepository extends JpaRepository<Course, UUID> {

    Page<Course> findByStatus(CourseStatus status, Pageable pageable);

    Page<Course> findByInstructorId(UUID instructorId, Pageable pageable);

    List<Course> findByInstructorIdAndStatus(UUID instructorId, CourseStatus status);

    @Query("SELECT c FROM Course c WHERE c.status = 'PUBLISHED' AND " +
            "(LOWER(c.title) LIKE LOWER(CONCAT('%', :search, '%')) OR " +
            "LOWER(c.description) LIKE LOWER(CONCAT('%', :search, '%')))")
    Page<Course> searchPublishedCourses(@Param("search") String search, Pageable pageable);

    @Query("SELECT c FROM Course c WHERE c.status = 'PUBLISHED' ORDER BY c.enrollmentCount DESC")
    List<Course> findPopularCourses(Pageable pageable);

    @Query("SELECT c FROM Course c WHERE c.featured = true AND c.status = 'PUBLISHED'")
    List<Course> findFeaturedCourses();

    @Query("SELECT c FROM Course c WHERE c.status = 'PUBLISHED' AND :tag MEMBER OF c.tags")
    Page<Course> findByTag(@Param("tag") String tag, Pageable pageable);

    @Query("SELECT c FROM Course c WHERE c.status = 'PUBLISHED' AND c.difficulty = :difficulty")
    Page<Course> findByDifficulty(@Param("difficulty") String difficulty, Pageable pageable);

    @Query("SELECT DISTINCT c.tags FROM Course c WHERE c.status = 'PUBLISHED'")
    List<String> findAllTags();

    @Query("SELECT COUNT(c) FROM Course c WHERE c.instructorId = :instructorId")
    long countByInstructorId(@Param("instructorId") UUID instructorId);
}
