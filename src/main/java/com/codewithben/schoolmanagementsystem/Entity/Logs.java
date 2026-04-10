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
                 LogStatus status, Staffs createdBy, String actionData) {
        this.actionId = actionId;
        this.actionTime = actionTime;
        this.actionType = actionType;
        this.status = status;
        this.createdBy = createdBy;
        this.actionData = actionData;
    }

    public String getActionId() {
        return actionId;
    }

    public LocalDateTime getActionTime() {
        return actionTime;
    }

    public LogType getActionType() {
        return actionType;
    }

    public LogStatus getStatus() {
        return status;
    }

    public Staffs getCreatedBy() {
        return createdBy;
    }

    public String getActionData() {
        return actionData;
    }
}
