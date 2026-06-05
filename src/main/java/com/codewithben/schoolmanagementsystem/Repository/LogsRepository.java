package com.codewithben.schoolmanagementsystem.Repository;

import com.codewithben.schoolmanagementsystem.Entity.Logs;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;

@Repository
public interface LogsRepository extends JpaRepository<Logs, Long> {

    List<Logs> findByInstitution_InstitutionIdAndActionDateOrderByActionIdDesc(
            String institutionId, LocalDate date
    );

    List<Logs> findByStaff_StaffIdAndActionDateBetweenOrderByActionIdDesc(
            String staffId, LocalDate startDate, LocalDate endDate
    );

    List<Logs> findByInstitution_InstitutionIdAndActionDateBetweenOrderByActionIdDesc(
            String institutionId, LocalDate startDate, LocalDate endDate
    );
}
