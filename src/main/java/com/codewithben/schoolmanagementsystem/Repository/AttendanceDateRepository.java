package com.codewithben.schoolmanagementsystem.Repository;

import com.codewithben.schoolmanagementsystem.Entity.AttendanceDate;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Repository
public interface AttendanceDateRepository extends JpaRepository<AttendanceDate, Integer> {

    Optional<AttendanceDate> findByLevel_LevelIDAndSemester_SemesterIDAndAttendanceDate(
            String levelId, String semesterId, LocalDate attendanceDate
    );

    List<AttendanceDate> findByAttendanceDateAndSemester_Institution_InstitutionId(
            LocalDate date, String institutionId
    );
}
