package com.codewithben.schoolmanagementsystem.Entity;

import com.codewithben.schoolmanagementsystem.Contants.LogStatus;
import com.codewithben.schoolmanagementsystem.Contants.LogType;
import jakarta.persistence.*;

import java.time.LocalDate;
import java.time.LocalTime;

@Entity
public class Logs {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long actionId;

    private LocalDate actionDate;

    private LocalTime actionTime;

    @Enumerated(EnumType.STRING)
    private LogType actionType;

    @Column(length = 5000)
    private String actionData;

    private String createdBy;

    @Enumerated(EnumType.STRING)
    private LogStatus status;

    public Logs() {}

    public Logs(long actionId, LocalDate actionDate, LocalTime actionTime, LogType actionType, String actionData, String createdBy, LogStatus status) {
        this.actionId = actionId;
        this.actionDate = actionDate;
        this.actionTime = actionTime;
        this.actionType = actionType;
        this.actionData = actionData;
        this.createdBy = createdBy;
        this.status = status;
    }

    public long getActionId() {
        return actionId;
    }

    public void setActionId(long actionId) {
        this.actionId = actionId;
    }

    public LocalDate getActionDate() {
        return actionDate;
    }

    public void setActionDate(LocalDate actionDate) {
        this.actionDate = actionDate;
    }

    public LocalTime getActionTime() {
        return actionTime;
    }

    public void setActionTime(LocalTime actionTime) {
        this.actionTime = actionTime;
    }

    public LogType getActionType() {
        return actionType;
    }

    public void setActionType(LogType actionType) {
        this.actionType = actionType;
    }

    public String getActionData() {
        return actionData;
    }

    public void setActionData(String actionData) {
        this.actionData = actionData;
    }

    public String getCreatedBy() {
        return createdBy;
    }

    public void setCreatedBy(String createdBy) {
        this.createdBy = createdBy;
    }

    public LogStatus getStatus() {
        return status;
    }

    public void setStatus(LogStatus status) {
        this.status = status;
    }
}
