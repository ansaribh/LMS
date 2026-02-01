package com.lms.assignment.kafka;

import com.lms.common.event.GradingJobEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.support.Acknowledgment;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class GradingJobConsumer {

    @KafkaListener(topics = "grading-results", groupId = "assignment-service")
    public void handleGradingResult(GradingJobEvent event, Acknowledgment ack) {
        try {
            log.info("Received grading result for submission: {}", event.getSubmissionId());
            // Process grading result - update submission status, notify student, etc.
            ack.acknowledge();
        } catch (Exception e) {
            log.error("Error processing grading result: {}", e.getMessage());
            // Handle retry logic
        }
    }
}
