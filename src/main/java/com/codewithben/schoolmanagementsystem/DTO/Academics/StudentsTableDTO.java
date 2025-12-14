package com.codewithben.schoolmanagementsystem.DTO.Academics;

public class StudentsTableDTO {
    private String studentId;

    private String fullName;

    private String gender;

    private String homeTown;

    private String parentName;

    private String parentPhone;

    public StudentsTableDTO(String studentId, String fullName, String gender, String homeTown, String parentName, String parentPhone) {
        this.studentId = studentId;
        this.fullName = fullName;
        this.gender = gender;
        this.homeTown = homeTown;
        this.parentName = parentName;
        this.parentPhone = parentPhone;
    }

    public String getStudentId() {
        return studentId;
    }

    public void setStudentId(String studentId) {
        this.studentId = studentId;
    }

    public String getFullName() {
        return fullName;
    }

    public void setFullName(String fullName) {
        this.fullName = fullName;
    }

    public String getGender() {
        return gender;
    }

    public void setGender(String gender) {
        this.gender = gender;
    }

    public String getHomeTown() {
        return homeTown;
    }

    public void setHomeTown(String homeTown) {
        this.homeTown = homeTown;
    }

    public String getParentName() {
        return parentName;
    }

    public void setParentName(String parentName) {
        this.parentName = parentName;
    }

    public String getParentPhone() {
        return parentPhone;
    }

    public void setParentPhone(String parentPhone) {
        this.parentPhone = parentPhone;
    }
}
