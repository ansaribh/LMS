package com.lms.course.repository;

import com.lms.course.entity.Lesson;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface LessonRepository extends JpaRepository<Lesson, UUID> {

    List<Lesson> findByModuleIdOrderByOrderIndexAsc(UUID moduleId);

    @Query("SELECT COALESCE(MAX(l.orderIndex), 0) FROM Lesson l WHERE l.module.id = :moduleId")
    int findMaxOrderIndexByModuleId(@Param("moduleId") UUID moduleId);

    Optional<Lesson> findByModuleIdAndId(UUID moduleId, UUID lessonId);

    @Query("SELECT l FROM Lesson l WHERE l.module.course.id = :courseId AND l.free = true")
    List<Lesson> findFreeLessonsByCourseId(@Param("courseId") UUID courseId);

    @Query("SELECT COUNT(l) FROM Lesson l WHERE l.module.course.id = :courseId")
    long countByCourseId(@Param("courseId") UUID courseId);
}
