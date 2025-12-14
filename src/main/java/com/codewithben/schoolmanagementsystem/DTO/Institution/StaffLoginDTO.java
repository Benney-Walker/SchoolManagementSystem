package com.codewithben.schoolmanagementsystem.DTO.Institution;

public class StaffLoginDTO {
    private String staffId;

    private String password;

    public StaffLoginDTO(String staffId, String password) {
        this.staffId = staffId;
        this.password = password;
    }

    public String getStaffId() {
        return staffId;
    }

    public void setStaffId(String staffId) {
        this.staffId = staffId;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }
}
