package com.lms.assignment.service;

import com.lms.assignment.entity.Assignment;
import com.lms.assignment.entity.Submission;
import com.lms.assignment.repository.AssignmentRepository;
import com.lms.assignment.repository.SubmissionRepository;
import com.lms.common.enums.SubmissionStatus;
import com.lms.common.event.GradingJobEvent;
import com.lms.common.exception.BadRequestException;
import com.lms.common.exception.ResourceNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class AssignmentService {
    private final AssignmentRepository assignmentRepository;
    private final SubmissionRepository submissionRepository;
    private final KafkaTemplate<String, Object> kafkaTemplate;
    private static final String GRADING_TOPIC = "grading-jobs";

    @Transactional(readOnly = true)
    public Assignment getAssignmentById(UUID id) {
        return assignmentRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Assignment", "id", id));
    }

    @Transactional(readOnly = true)
    public List<Assignment> getAssignmentsByCourse(UUID courseId) {
        return assignmentRepository.findByCourseId(courseId);
    }

    @Transactional
    public Assignment createAssignment(Assignment assignment) {
        assignment = assignmentRepository.save(assignment);
        log.info("Assignment created: {}", assignment.getId());
        return assignment;
    }

    @Transactional
    public Assignment updateAssignment(UUID id, Assignment updated) {
        Assignment assignment = getAssignmentById(id);
        if (updated.getTitle() != null) assignment.setTitle(updated.getTitle());
        if (updated.getDescription() != null) assignment.setDescription(updated.getDescription());
        if (updated.getMaxScore() != null) assignment.setMaxScore(updated.getMaxScore());
        if (updated.getDueDate() != null) assignment.setDueDate(updated.getDueDate());
        return assignmentRepository.save(assignment);
    }

    @Transactional
    public Submission submitAssignment(UUID assignmentId, UUID studentId, String content, String fileUrl) {
        Assignment assignment = getAssignmentById(assignmentId);
        int attempts = submissionRepository.countAttempts(assignmentId, studentId);
        if (attempts >= assignment.getMaxAttempts()) {
            throw new BadRequestException("Maximum attempts reached");
        }
        boolean isLate = assignment.getDueDate() != null && LocalDateTime.now().isAfter(assignment.getDueDate());
        if (isLate && !assignment.isAllowLateSubmission()) {
            throw new BadRequestException("Late submissions not allowed");
        }
        Submission submission = Submission.builder()
                .assignment(assignment).studentId(studentId).content(content).fileUrl(fileUrl)
                .attemptNumber(attempts + 1).late(isLate).build();
        submission.submit();
        submission = submissionRepository.save(submission);
        
        // Send grading job to Kafka
        GradingJobEvent event = GradingJobEvent.create(submission.getId(), assignmentId, studentId, 
                assignment.getCourseId(), "ASSIGNMENT", false);
        kafkaTemplate.send(GRADING_TOPIC, submission.getId().toString(), event);
        log.info("Submission created and grading job sent: {}", submission.getId());
        return submission;
    }

    @Transactional
    public Submission gradeSubmission(UUID submissionId, Integer score, String feedback, UUID gradedBy) {
        Submission submission = submissionRepository.findById(submissionId)
                .orElseThrow(() -> new ResourceNotFoundException("Submission", "id", submissionId));
        Assignment assignment = submission.getAssignment();
        if (submission.isLate() && assignment.getLatePenaltyPercent() != null) {
            int penalty = (score * assignment.getLatePenaltyPercent()) / 100;
            score = Math.max(0, score - penalty);
        }
        submission.grade(score, feedback, gradedBy);
        submission = submissionRepository.save(submission);
        log.info("Submission graded: {} with score {}", submissionId, score);
        return submission;
    }

    @Transactional(readOnly = true)
    public List<Submission> getSubmissionsByAssignment(UUID assignmentId) {
        return submissionRepository.findByAssignmentId(assignmentId);
    }

    @Transactional(readOnly = true)
    public List<Submission> getPendingSubmissions(UUID assignmentId) {
        return submissionRepository.findByAssignmentIdAndStatus(assignmentId, SubmissionStatus.SUBMITTED);
    }
}
