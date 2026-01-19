package com.codewithben.schoolmanagementsystem.DTO.Institution;

public class StudentAttendance {
    private String levelId;

    private String studentId;

    private String studentName;

    private String status;

    public StudentAttendance(String levelId, String studentId, String studentName, String status) {
        this.levelId = levelId;
        this.studentId = studentId;
        this.studentName = studentName;
        this.status = status;
    }

    public String getLevelId() {
        return levelId;
    }

    public void setLevelId(String levelId) {
        this.levelId = levelId;
    }

    public String getStudentId() {
        return studentId;
    }

    public void setStudentId(String studentId) {
        this.studentId = studentId;
    }

    public String getStudentName() {
        return studentName;
    }

    public void setStudentName(String studentName) {
        this.studentName = studentName;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }
}
