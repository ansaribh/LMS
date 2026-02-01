package com.lms.common.event;

import com.lms.common.enums.AttendanceStatus;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

import java.time.LocalDate;
import java.util.Set;
import java.util.UUID;

@Data
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = true)
public class AttendanceEvent extends BaseEvent {
    
    private UUID courseId;
    private String courseName;
    private LocalDate date;
    private UUID studentId;
    private String studentName;
    private Set<UUID> parentIds;
    private AttendanceStatus status;
    private String remarks;
    private UUID markedBy;

    public static AttendanceEvent create(UUID courseId, String courseName, LocalDate date,
                                          UUID studentId, String studentName, Set<UUID> parentIds,
                                          AttendanceStatus status, UUID markedBy) {
        AttendanceEvent event = AttendanceEvent.builder()
                .courseId(courseId)
                .courseName(courseName)
                .date(date)
                .studentId(studentId)
                .studentName(studentName)
                .parentIds(parentIds)
                .status(status)
                .markedBy(markedBy)
                .build();
        event.initializeEvent("ATTENDANCE_MARKED", "attendance-service");
        return event;
    }
}
