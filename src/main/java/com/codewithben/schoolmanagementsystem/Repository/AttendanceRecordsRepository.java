package com.codewithben.schoolmanagementsystem.Repository;

import com.codewithben.schoolmanagementsystem.Constants.AttendanceStatus;
import com.codewithben.schoolmanagementsystem.Entity.AttendanceRecords;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Repository
public interface AttendanceRecordsRepository extends JpaRepository<AttendanceRecords, Long> {
    Optional<AttendanceRecords> findByAttendanceDate_DateIdAndStudent_StudentId(
            int attendanceDate, String studentId
    );

    List<AttendanceRecords> findByStudent_StudentIdAndStatusAndAttendanceDate_Semester_SemesterID(
            String studentId, AttendanceStatus status, String semesterId
    );
}
