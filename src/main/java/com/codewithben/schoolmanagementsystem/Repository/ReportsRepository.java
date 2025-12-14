package com.codewithben.schoolmanagementsystem.Repository;

import com.codewithben.schoolmanagementsystem.Entity.Reports;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface ReportsRepository extends JpaRepository<Reports, Long> {
    public List<Reports> findReportsByStatus(String status);

    List<Reports> findByCreationDateBetween(LocalDateTime from, LocalDateTime to);
}
