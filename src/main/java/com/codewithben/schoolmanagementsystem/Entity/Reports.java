package com.codewithben.schoolmanagementsystem.Entity;

import jakarta.persistence.*;

import java.time.LocalDateTime;

@Entity
public class Reports {

    @Id
    private String reportId;

    @ManyToOne
    private Staffs staff;

    private LocalDateTime creationDate;

    @Column(length = 1000)
    private String reportData;

    private String constraint;

    private String priority;

    private String status;

    public String getReportId() {
        return reportId;
    }

    public void setReportId(String reportId) {
        this.reportId = reportId;
    }

    public Staffs getStaff() {
        return staff;
    }

    public void setStaff(Staffs instructorId) {
        this.staff = instructorId;
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

    public String getConstraint() {
        return constraint;
    }

    public void setConstraint(String constraint) {
        this.constraint = constraint;
    }

    public String getPriority() {
        return priority;
    }

    public void setPriority(String priority) {
        this.priority = priority;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }
}
