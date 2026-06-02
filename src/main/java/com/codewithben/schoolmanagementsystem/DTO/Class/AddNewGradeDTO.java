package com.codewithben.schoolmanagementsystem.DTO.Class;

public class AddNewGradeDTO {
    private String gradeName;

    private String staffId;

    public AddNewGradeDTO(String gradeName, String staffId) {
        this.gradeName = gradeName;
        this.staffId = staffId;
    }

    public String getGradeName() {
        return gradeName;
    }

    public void setGradeName(String gradeName) {
        this.gradeName = gradeName;
    }

    public String getStaffId() {
        return staffId;
    }

    public void setStaffId(String staffId) {
        this.staffId = staffId;
    }
}
