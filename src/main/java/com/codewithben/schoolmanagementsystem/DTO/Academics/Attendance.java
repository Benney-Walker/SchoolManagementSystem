package com.codewithben.schoolmanagementsystem.DTO.Academics;

public class Attendance {
    private String levelId;

    private String staffId;

    private String studentId;

    private String status;

    public Attendance(String studentId, String levelId, String staffId, String status) {
        this.studentId = studentId;
        this.levelId = levelId;
        this.staffId = staffId;
        this.status = status;
    }

    public String getStudentId() {
        return studentId;
    }

    public void setStudentId(String studentId) {
        this.studentId = studentId;
    }

    public String getLevelId() {
        return levelId;
    }

    public void setLevelId(String levelId) {
        this.levelId = levelId;
    }

    public String getStaffId() {
        return staffId;
    }

    public void setStaffId(String staffId) {
        this.staffId = staffId;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }
}
