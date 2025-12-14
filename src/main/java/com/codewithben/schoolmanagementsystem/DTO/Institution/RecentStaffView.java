package com.codewithben.schoolmanagementsystem.DTO.Institution;

import com.codewithben.schoolmanagementsystem.Entity.Subjects;

import java.util.List;

public class RecentStaffView {
    String staffId;

    String staffName;

    String assignedLevel;

    public RecentStaffView(String staffId, String staffName, String assignedLevel) {
        this.staffName = staffName;
        this.staffId = staffId;
        this.assignedLevel = assignedLevel;
    }

    public String getStaffName() {
        return staffName;
    }

    public void setStaffName(String staffName) {
        this.staffName = staffName;
    }

    public String getStaffId() {
        return staffId;
    }

    public void setStaffId(String staffId) {
        this.staffId = staffId;
    }

    public String getAssignedLevel() {
        return assignedLevel;
    }

    public void setAssignedLevel(String assignedLevel) {
        this.assignedLevel = assignedLevel;
    }
}
