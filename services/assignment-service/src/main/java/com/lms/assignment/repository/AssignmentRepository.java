package com.lms.assignment.repository;

import com.lms.assignment.entity.Assignment;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.UUID;

@Repository
public interface AssignmentRepository extends JpaRepository<Assignment, UUID> {
    List<Assignment> findByCourseId(UUID courseId);
    Page<Assignment> findByCourseId(UUID courseId, Pageable pageable);
    List<Assignment> findByCourseIdAndPublishedTrue(UUID courseId);
    List<Assignment> findByInstructorId(UUID instructorId);
    long countByCourseId(UUID courseId);
}
