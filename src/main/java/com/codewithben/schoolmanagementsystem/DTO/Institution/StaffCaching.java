package com.codewithben.schoolmanagementsystem.DTO.Institution;

public class StaffCaching {
    private String staffName;

    private String staffId;

    public StaffCaching(String staffName, String staffId) {
        this.staffName = staffName;
        this.staffId = staffId;
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
}
