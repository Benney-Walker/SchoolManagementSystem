package com.codewithben.schoolmanagementsystem.Entity;

import com.codewithben.schoolmanagementsystem.Contants.LogStatus;
import com.codewithben.schoolmanagementsystem.Contants.LogType;
import jakarta.persistence.*;

import java.time.LocalDateTime;

@Entity
public class Logs {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long actionId;

    private LocalDateTime actionTime;

    @Enumerated(EnumType.STRING)
    private LogType actionType;

    @Column(length = 5000)
    private String actionData;

    private String createdBy;

    @Enumerated(EnumType.STRING)
    private LogStatus status;

    public Logs() {}

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
