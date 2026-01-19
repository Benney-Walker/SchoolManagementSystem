package com.codewithben.schoolmanagementsystem.DTO.Institution;

public class StaffCaching {
    private String staffName;

    private String staffId;

    private String staffRole;

    public StaffCaching(String staffName, String staffId,  String staffRole) {
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

    public String getStaffRole() {
        return staffRole;
    }

    public void setStaffRole(String staffRole) {
        this.staffRole = staffRole;
    }
}
