package com.codewithben.schoolmanagementsystem.Entity;

import com.codewithben.schoolmanagementsystem.Contants.LogStatus;
import com.codewithben.schoolmanagementsystem.Contants.LogType;
import jakarta.persistence.*;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
public class Logs {

    @Id
    private String actionId;

    private LocalDateTime actionTime;

    @Enumerated(EnumType.STRING)
    private LogType actionType;

    @Column(length = 5000)
    private String actionData;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(nullable = false, name = "createdBy_staffId")
    private Staffs createdBy;

    @Enumerated(EnumType.STRING)
    private LogStatus status;

    public Logs() {}

    public Logs (String actionId, LocalDateTime actionTime, LogType actionType,
                 LogStatus status, String actionData) {
        this.actionId = actionId;
        this.actionTime = actionTime;
        this.actionType = actionType;
        this.status = status;
        this.actionData = actionData;
    }

    public String getActionId() {
        return actionId;
    }

    public void setActionId(String actionId) {
        this.actionId = actionId;
    }

    public LocalDateTime getActionTime() {
        return actionTime;
    }

    public void setActionTime(LocalDateTime actionTime) {
        this.actionTime = actionTime;
    }

    public LogType getActionType() {
        return actionType;
    }

    public void setActionType(LogType actionType) {
        this.actionType = actionType;
    }

    public LogStatus getStatus() {
        return status;
    }

    public void setStatus(LogStatus status) {
        this.status = status;
    }

    public Staffs getCreatedBy() {
        return createdBy;
    }

    public void setCreatedBy(Staffs createdBy) {
        this.createdBy = createdBy;
    }

    public String getActionData() {
        return actionData;
    }

    public void setActionData(String message) {
        this.actionData = message;
    }
}
