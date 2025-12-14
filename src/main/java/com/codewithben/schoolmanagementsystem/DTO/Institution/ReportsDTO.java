package com.codewithben.schoolmanagementsystem.DTO.Institution;

import com.codewithben.schoolmanagementsystem.Entity.Staffs;

import java.time.LocalDateTime;

public class ReportsDTO {

    private Staffs instructor;

    private LocalDateTime creationDate;

    private String reportData;

    public ReportsDTO(Staffs instructor, LocalDateTime creationDate, String reportData) {
        this.instructor = instructor;
        this.creationDate = creationDate;
        this.reportData = reportData;
    }

    public Staffs getInstructor() {
        return instructor;
    }

    public void setInstructor(Staffs instructor) {
        this.instructor = instructor;
    }

    public LocalDateTime getCreationDate() {
        return creationDate;
    }

    public void setCreationDate(LocalDateTime creationDate) {
        this.creationDate = creationDate;
    }

    public String getReportData() {
        return reportData;
    }

    public void setReportData(String reportData) {
        this.reportData = reportData;
    }
}
