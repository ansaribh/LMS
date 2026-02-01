package com.lms.assignment.repository;

import com.lms.assignment.entity.Submission;
import com.lms.common.enums.SubmissionStatus;
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
public interface SubmissionRepository extends JpaRepository<Submission, UUID> {
    List<Submission> findByAssignmentId(UUID assignmentId);
    Page<Submission> findByAssignmentId(UUID assignmentId, Pageable pageable);
    List<Submission> findByStudentId(UUID studentId);
    Optional<Submission> findByAssignmentIdAndStudentId(UUID assignmentId, UUID studentId);
    List<Submission> findByAssignmentIdAndStatus(UUID assignmentId, SubmissionStatus status);
    @Query("SELECT COUNT(s) FROM Submission s WHERE s.assignment.id = :assignmentId AND s.studentId = :studentId")
    int countAttempts(@Param("assignmentId") UUID assignmentId, @Param("studentId") UUID studentId);
    @Query("SELECT AVG(s.score) FROM Submission s WHERE s.assignment.id = :assignmentId AND s.status = 'GRADED'")
    Double getAverageScore(@Param("assignmentId") UUID assignmentId);
}
