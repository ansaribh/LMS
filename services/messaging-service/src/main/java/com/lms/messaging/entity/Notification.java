package com.lms.messaging.entity;

import com.lms.common.entity.BaseEntity;
import com.lms.common.enums.NotificationType;
import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.SuperBuilder;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity @Table(name = "notifications")
@Getter @Setter @SuperBuilder @NoArgsConstructor @AllArgsConstructor
public class Notification extends BaseEntity {
    @Column(name = "recipient_id", nullable = false) private UUID recipientId;
    @Column(name = "recipient_email") private String recipientEmail;
    @Enumerated(EnumType.STRING)
    @Column(name = "type", nullable = false) private NotificationType type;
    @Column(name = "subject") private String subject;
    @Column(name = "body", length = 5000) private String body;
    @Column(name = "is_read") @Builder.Default private boolean read = false;
    @Column(name = "sent_at") private LocalDateTime sentAt;
    @Column(name = "read_at") private LocalDateTime readAt;
    @Column(name = "priority") @Builder.Default private String priority = "MEDIUM";
}
