package com.lms.common.event;

import com.lms.common.enums.NotificationType;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

import java.util.Map;
import java.util.Set;
import java.util.UUID;

@Data
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = true)
public class NotificationEvent extends BaseEvent {
    
    private Set<UUID> recipientIds;
    private String recipientEmail;
    private String recipientPhone;
    private NotificationType type;
    private String templateId;
    private String subject;
    private String body;
    private Map<String, Object> templateData;
    private String priority; // HIGH, MEDIUM, LOW

    public static NotificationEvent createEmail(Set<UUID> recipientIds, String subject, 
                                                 String templateId, Map<String, Object> data) {
        NotificationEvent event = NotificationEvent.builder()
                .recipientIds(recipientIds)
                .type(NotificationType.EMAIL)
                .subject(subject)
                .templateId(templateId)
                .templateData(data)
                .priority("MEDIUM")
                .build();
        event.initializeEvent("NOTIFICATION_EMAIL", "messaging-service");
        return event;
    }

    public static NotificationEvent createPush(Set<UUID> recipientIds, String subject, 
                                                String body) {
        NotificationEvent event = NotificationEvent.builder()
                .recipientIds(recipientIds)
                .type(NotificationType.PUSH)
                .subject(subject)
                .body(body)
                .priority("MEDIUM")
                .build();
        event.initializeEvent("NOTIFICATION_PUSH", "messaging-service");
        return event;
    }
}
