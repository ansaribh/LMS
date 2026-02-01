package com.lms.messaging.service;

import com.lms.common.enums.NotificationType;
import com.lms.common.event.NotificationEvent;
import com.lms.messaging.entity.Notification;
import com.lms.messaging.repository.NotificationRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Slf4j @Service @RequiredArgsConstructor
public class NotificationService {
    private final NotificationRepository notificationRepository;
    private final JavaMailSender mailSender;

    @KafkaListener(topics = {"notifications", "attendance-events"}, groupId = "messaging-service")
    public void handleNotificationEvent(NotificationEvent event) {
        log.info("Received notification event: {}", event.getEventType());
        for (UUID recipientId : event.getRecipientIds()) {
            Notification notification = Notification.builder()
                    .recipientId(recipientId).type(event.getType()).subject(event.getSubject())
                    .body(event.getBody()).priority(event.getPriority()).sentAt(LocalDateTime.now()).build();
            notificationRepository.save(notification);
            if (event.getType() == NotificationType.EMAIL && event.getRecipientEmail() != null) {
                sendEmail(event.getRecipientEmail(), event.getSubject(), event.getBody());
            }
        }
    }

    @Transactional
    public Notification sendNotification(UUID recipientId, String email, NotificationType type, String subject, String body) {
        Notification notification = Notification.builder()
                .recipientId(recipientId).recipientEmail(email).type(type).subject(subject).body(body).sentAt(LocalDateTime.now()).build();
        if (type == NotificationType.EMAIL && email != null) sendEmail(email, subject, body);
        return notificationRepository.save(notification);
    }

    private void sendEmail(String to, String subject, String body) {
        try {
            SimpleMailMessage message = new SimpleMailMessage();
            message.setTo(to);
            message.setSubject(subject);
            message.setText(body);
            mailSender.send(message);
            log.info("Email sent to: {}", to);
        } catch (Exception e) {
            log.error("Failed to send email: {}", e.getMessage());
        }
    }

    @Transactional(readOnly = true)
    public List<Notification> getUserNotifications(UUID userId) {
        return notificationRepository.findByRecipientIdOrderByCreatedAtDesc(userId);
    }

    @Transactional
    public Notification markAsRead(UUID notificationId) {
        Notification n = notificationRepository.findById(notificationId).orElseThrow();
        n.setRead(true);
        n.setReadAt(LocalDateTime.now());
        return notificationRepository.save(n);
    }

    @Transactional(readOnly = true)
    public long getUnreadCount(UUID userId) {
        return notificationRepository.countByRecipientIdAndReadFalse(userId);
    }
}
