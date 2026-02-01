package com.lms.quiz.repository;

import com.lms.quiz.entity.QuizAttempt;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.UUID;

@Repository
public interface QuizAttemptRepository extends JpaRepository<QuizAttempt, UUID> {
    List<QuizAttempt> findByQuizIdAndStudentId(UUID quizId, UUID studentId);
    List<QuizAttempt> findByStudentId(UUID studentId);
    @Query("SELECT COUNT(a) FROM QuizAttempt a WHERE a.quiz.id = :quizId AND a.studentId = :studentId")
    int countAttempts(@Param("quizId") UUID quizId, @Param("studentId") UUID studentId);
    @Query("SELECT AVG(a.percentage) FROM QuizAttempt a WHERE a.quiz.id = :quizId AND a.submittedAt IS NOT NULL")
    Double getAverageScore(@Param("quizId") UUID quizId);
}
