package com.codewithben.schoolmanagementsystem.Repository;

import com.codewithben.schoolmanagementsystem.Entity.FeesReport;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface FeesReportRepository extends JpaRepository<FeesReport, Long> {
    Optional<FeesReport> findByFeesReportId(String id);

    List<FeesReport> findByStudent_StudentIdAndFees_FeesId(String studentId, int feesId);

    List<FeesReport> findByInstitution_InstitutionIdOrderByDateOfPaymentDesc(String institutionId);
}
