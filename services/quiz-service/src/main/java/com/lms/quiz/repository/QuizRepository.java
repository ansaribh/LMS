package com.lms.quiz.repository;

import com.lms.quiz.entity.Quiz;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.UUID;

@Repository
public interface QuizRepository extends JpaRepository<Quiz, UUID> {
    List<Quiz> findByCourseId(UUID courseId);
    List<Quiz> findByCourseIdAndPublishedTrue(UUID courseId);
    List<Quiz> findByInstructorId(UUID instructorId);
}
