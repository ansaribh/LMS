package com.lms.attendance.entity;

import com.lms.common.entity.BaseEntity;
import com.lms.common.enums.AttendanceStatus;
import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.SuperBuilder;
import java.time.LocalDate;
import java.util.UUID;

@Entity @Table(name = "attendance", uniqueConstraints = @UniqueConstraint(columnNames = {"course_id", "student_id", "date"}))
@Getter @Setter @SuperBuilder @NoArgsConstructor @AllArgsConstructor
public class Attendance extends BaseEntity {
    @Column(name = "course_id", nullable = false) private UUID courseId;
    @Column(name = "student_id", nullable = false) private UUID studentId;
    @Column(name = "date", nullable = false) private LocalDate date;
    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false) private AttendanceStatus status;
    @Column(name = "marked_by", nullable = false) private UUID markedBy;
    @Column(name = "remarks", length = 500) private String remarks;
}
