package com.codewithben.schoolmanagementsystem.Repository;

import com.codewithben.schoolmanagementsystem.Contants.AttendanceStatus;
import com.codewithben.schoolmanagementsystem.Entity.Attendance;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;

@Repository
public interface AttendanceRepository extends JpaRepository<Attendance, Long> {
    List<Attendance> findByLevel_LevelIDAndDateMarked(
            String levelId, LocalDate dateMarked
    );

    List<Attendance> findByStatusAndDateMarkedAndInstitution_InstitutionId(
            AttendanceStatus status, LocalDate dateMarked, String institutionId
    );
}
