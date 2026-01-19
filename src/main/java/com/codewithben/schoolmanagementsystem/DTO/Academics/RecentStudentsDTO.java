package com.codewithben.schoolmanagementsystem.DTO.Academics;

public class RecentStudentsDTO {
    private String studentId;

    private String studentName;

    private String studentGrade;

    private String dateOfRegistration;

    private String status;

    public RecentStudentsDTO(String studentId, String studentName, String studentGrade, String dateOfRegistration,
                             String status) {
        this.studentId = studentId;
        this.studentName = studentName;
        this.studentGrade = studentGrade;
        this.dateOfRegistration = dateOfRegistration;
        this.status = status;
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

    public String getStudentGrade() {
        return studentGrade;
    }

    public void setStudentGrade(String studentGrade) {
        this.studentGrade = studentGrade;
    }

    public String getDateOfRegistration() {
        return dateOfRegistration;
    }

    public void setDateOfRegistration(String dateOfRegistration) {
        this.dateOfRegistration = dateOfRegistration;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }
}
