package com.lms.attendance.repository;

import com.lms.attendance.entity.Attendance;
import com.lms.common.enums.AttendanceStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface AttendanceRepository extends JpaRepository<Attendance, UUID> {
    List<Attendance> findByCourseIdAndDate(UUID courseId, LocalDate date);
    List<Attendance> findByStudentId(UUID studentId);
    List<Attendance> findByStudentIdAndCourseId(UUID studentId, UUID courseId);
    Optional<Attendance> findByCourseIdAndStudentIdAndDate(UUID courseId, UUID studentId, LocalDate date);
    @Query("SELECT COUNT(a) FROM Attendance a WHERE a.studentId = :studentId AND a.courseId = :courseId AND a.status = :status")
    long countByStudentAndStatus(@Param("studentId") UUID studentId, @Param("courseId") UUID courseId, @Param("status") AttendanceStatus status);
    @Query("SELECT COUNT(a) FROM Attendance a WHERE a.studentId = :studentId AND a.courseId = :courseId")
    long countTotalByStudentAndCourse(@Param("studentId") UUID studentId, @Param("courseId") UUID courseId);
}
