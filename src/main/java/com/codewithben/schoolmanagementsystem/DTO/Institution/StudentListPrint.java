package com.codewithben.schoolmanagementsystem.DTO.Institution;

public class StudentListPrint {
    private String studentId;

    private String fullName;

    public StudentListPrint(String studentId, String fullName) {
        this.studentId = studentId;
        this.fullName = fullName;
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
}
