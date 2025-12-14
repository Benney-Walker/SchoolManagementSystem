package com.codewithben.schoolmanagementsystem.Repository;

import com.codewithben.schoolmanagementsystem.Entity.FeesReport;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface FeesReportRepository extends JpaRepository<FeesReport, Long> {
    List<FeesReport> findByStudent_StudentIdAndFees_FeesId(String studentId, int feesId);

    List<FeesReport> findByFees_FeesId(int feesId);
}
