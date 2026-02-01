package com.lms.quiz.service;

import com.lms.common.exception.BadRequestException;
import com.lms.common.exception.ResourceNotFoundException;
import com.lms.quiz.entity.*;
import com.lms.quiz.repository.QuizAttemptRepository;
import com.lms.quiz.repository.QuizRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Slf4j @Service @RequiredArgsConstructor
public class QuizService {
    private final QuizRepository quizRepository;
    private final QuizAttemptRepository attemptRepository;

    @Transactional(readOnly = true)
    public Quiz getQuizById(UUID id) {
        return quizRepository.findById(id).orElseThrow(() -> new ResourceNotFoundException("Quiz", "id", id));
    }

    @Transactional(readOnly = true)
    public List<Quiz> getQuizzesByCourse(UUID courseId) {
        return quizRepository.findByCourseId(courseId);
    }

    @Transactional
    public Quiz createQuiz(Quiz quiz) {
        quiz = quizRepository.save(quiz);
        log.info("Quiz created: {}", quiz.getId());
        return quiz;
    }

    @Transactional
    public QuizAttempt startAttempt(UUID quizId, UUID studentId) {
        Quiz quiz = getQuizById(quizId);
        int attempts = attemptRepository.countAttempts(quizId, studentId);
        if (attempts >= quiz.getMaxAttempts()) throw new BadRequestException("Maximum attempts reached");
        QuizAttempt attempt = QuizAttempt.builder()
                .quiz(quiz).studentId(studentId).startedAt(LocalDateTime.now()).attemptNumber(attempts + 1).build();
        return attemptRepository.save(attempt);
    }

    @Transactional
    public QuizAttempt submitAttempt(UUID attemptId, Map<UUID, String> answers) {
        QuizAttempt attempt = attemptRepository.findById(attemptId)
                .orElseThrow(() -> new ResourceNotFoundException("Attempt", "id", attemptId));
        Quiz quiz = attempt.getQuiz();
        for (Question q : quiz.getQuestions()) {
            String ans = answers.get(q.getId());
            boolean correct = isAnswerCorrect(q, ans);
            QuizAnswer qa = QuizAnswer.builder().attempt(attempt).question(q).answer(ans).correct(correct).build();
            attempt.getAnswers().add(qa);
        }
        attempt.setSubmittedAt(LocalDateTime.now());
        attempt.calculateScore(quiz.getPassingScore());
        log.info("Quiz attempt submitted: {} with score {}%", attemptId, attempt.getPercentage());
        return attemptRepository.save(attempt);
    }

    private boolean isAnswerCorrect(Question q, String answer) {
        if (answer == null) return false;
        return switch (q.getType()) {
            case MULTIPLE_CHOICE, TRUE_FALSE -> q.getOptions().stream()
                    .anyMatch(o -> o.isCorrect() && o.getText().equalsIgnoreCase(answer));
            case SHORT_ANSWER, FILL_IN_BLANK -> q.getCorrectAnswer() != null && 
                    q.getCorrectAnswer().trim().equalsIgnoreCase(answer.trim());
            default -> false;
        };
    }

    @Transactional(readOnly = true)
    public List<QuizAttempt> getStudentAttempts(UUID quizId, UUID studentId) {
        return attemptRepository.findByQuizIdAndStudentId(quizId, studentId);
    }
}
