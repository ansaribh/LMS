package com.lms.course.repository;

import com.lms.course.entity.Module;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface ModuleRepository extends JpaRepository<Module, UUID> {

    List<Module> findByCourseIdOrderByOrderIndexAsc(UUID courseId);

    @Query("SELECT COALESCE(MAX(m.orderIndex), 0) FROM Module m WHERE m.course.id = :courseId")
    int findMaxOrderIndexByCourseId(@Param("courseId") UUID courseId);

    Optional<Module> findByCourseIdAndId(UUID courseId, UUID moduleId);

    @Query("SELECT m FROM Module m LEFT JOIN FETCH m.lessons WHERE m.id = :moduleId")
    Optional<Module> findByIdWithLessons(@Param("moduleId") UUID moduleId);
}
