package com.codewithben.schoolmanagementsystem.DTO.Academics;

import java.time.LocalDate;

public class UpdateStudentPersonalData {
    private String studentId;

    private String gender;

    private LocalDate dateOfBirth;

    private String guardianName;

    private String guardianContact;

    private String gradeName;

    private String status;

    public String getStudentId() {
        return studentId;
    }

    public String getGender() {
        return gender;
    }

    public LocalDate getDateOfBirth() {
        return dateOfBirth;
    }

    public String getGuardianName() {
        return guardianName;
    }

    public String getGuardianContact() {
        return guardianContact;
    }

    public String getGradeName() {
        return gradeName;
    }

    public String getStatus() {
        return status;
    }

    public void setStudentId(String studentId) {
        this.studentId = studentId;
    }
}
