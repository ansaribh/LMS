package com.lms.attendance.service;

import com.lms.attendance.entity.Attendance;
import com.lms.attendance.repository.AttendanceRepository;
import com.lms.common.enums.AttendanceStatus;
import com.lms.common.event.AttendanceEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.time.LocalDate;
import java.util.List;
import java.util.Set;
import java.util.UUID;

@Slf4j @Service @RequiredArgsConstructor
public class AttendanceService {
    private final AttendanceRepository attendanceRepository;
    private final KafkaTemplate<String, Object> kafkaTemplate;
    private static final String ATTENDANCE_TOPIC = "attendance-events";

    @Transactional
    public Attendance markAttendance(UUID courseId, UUID studentId, LocalDate date, AttendanceStatus status, UUID markedBy, String remarks) {
        Attendance attendance = attendanceRepository.findByCourseIdAndStudentIdAndDate(courseId, studentId, date)
                .orElse(Attendance.builder().courseId(courseId).studentId(studentId).date(date).build());
        attendance.setStatus(status);
        attendance.setMarkedBy(markedBy);
        attendance.setRemarks(remarks);
        attendance = attendanceRepository.save(attendance);

        // Send notification event for absent/late students
        if (status == AttendanceStatus.ABSENT || status == AttendanceStatus.LATE) {
            AttendanceEvent event = AttendanceEvent.create(courseId, "Course", date, studentId, "Student", Set.of(), status, markedBy);
            kafkaTemplate.send(ATTENDANCE_TOPIC, studentId.toString(), event);
            log.info("Attendance notification sent for student {} - status: {}", studentId, status);
        }
        return attendance;
    }

    @Transactional(readOnly = true)
    public List<Attendance> getAttendanceByDate(UUID courseId, LocalDate date) {
        return attendanceRepository.findByCourseIdAndDate(courseId, date);
    }

    @Transactional(readOnly = true)
    public List<Attendance> getStudentAttendance(UUID studentId) {
        return attendanceRepository.findByStudentId(studentId);
    }

    @Transactional(readOnly = true)
    public double getAttendancePercentage(UUID studentId, UUID courseId) {
        long total = attendanceRepository.countTotalByStudentAndCourse(studentId, courseId);
        if (total == 0) return 100.0;
        long present = attendanceRepository.countByStudentAndStatus(studentId, courseId, AttendanceStatus.PRESENT);
        return (present * 100.0) / total;
    }
}
