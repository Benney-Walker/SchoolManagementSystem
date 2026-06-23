package com.codewithben.schoolmanagementsystem.DTO.Attendance;

public class MarkAttendance_List {
    private String levelId;

    private String studentId;

    private String status;

    public MarkAttendance_List(String studentId, String levelId, String status) {
        this.studentId = studentId;
        this.levelId = levelId;
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

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }
}
