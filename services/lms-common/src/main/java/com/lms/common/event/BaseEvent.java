package com.lms.common.event;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

import java.time.LocalDateTime;
import java.util.UUID;

@Data
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
public abstract class BaseEvent {
    
    private UUID eventId;
    private String eventType;
    private LocalDateTime timestamp;
    private String source;
    private UUID correlationId;

    protected void initializeEvent(String eventType, String source) {
        this.eventId = UUID.randomUUID();
        this.eventType = eventType;
        this.timestamp = LocalDateTime.now();
        this.source = source;
        this.correlationId = UUID.randomUUID();
    }
}
