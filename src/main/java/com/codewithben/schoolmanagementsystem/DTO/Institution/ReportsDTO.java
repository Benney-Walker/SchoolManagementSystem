package com.codewithben.schoolmanagementsystem.DTO.Institution;

import com.codewithben.schoolmanagementsystem.Entity.Staffs;

import java.time.LocalDate;

public class ReportsDTO {

    private Staffs instructor;

    private LocalDate creationDate;

    private String reportData;

    public ReportsDTO(Staffs instructor, LocalDate creationDate, String reportData) {
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

    public LocalDate getCreationDate() {
        return creationDate;
    }

    public void setCreationDate(LocalDate creationDate) {
        this.creationDate = creationDate;
    }

    public String getReportData() {
        return reportData;
    }

    public void setReportData(String reportData) {
        this.reportData = reportData;
    }
}
