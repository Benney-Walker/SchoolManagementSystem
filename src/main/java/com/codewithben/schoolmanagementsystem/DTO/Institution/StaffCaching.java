package com.codewithben.schoolmanagementsystem.DTO.Institution;

import java.util.List;

public class StaffCaching {
    private String staffName;

    private String staffId;

    private List<String> staffRoles;

    public StaffCaching(String staffName, String staffId, List<String> staffRoles) {
        this.staffName = staffName;
        this.staffId = staffId;
        this.staffRoles = staffRoles;
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

    public List<String> getStaffRoles() {
        return staffRoles;
    }

    public void setStaffRoles(List<String> staffRoles) {
        this.staffRoles = staffRoles;
    }
}
