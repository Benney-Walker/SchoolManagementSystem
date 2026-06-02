package com.codewithben.schoolmanagementsystem.DTO.Staff;

import java.util.List;

public class ViewStaffList {
    private String staffId;

    private String staffName;

    private List<String> staffRole;

    public ViewStaffList() {}

    public ViewStaffList(String staffId, String staffName,
                         List<String> staffRole) {
        this.staffName = staffName;
        this.staffId = staffId;
        this.staffRole = staffRole;
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

    public List<String> getStaffRole() {
        return staffRole;
    }

    public void setStaffRole(List<String> staffRole) {
        this.staffRole = staffRole;
    }
}
