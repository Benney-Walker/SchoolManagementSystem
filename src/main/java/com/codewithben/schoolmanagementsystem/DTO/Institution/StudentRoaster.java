package com.codewithben.schoolmanagementsystem.DTO.Institution;

public class StudentRoaster {
    private String studentId;

    private String studentName;

    private String studentGender;

    private String homeTown;

    private String guardianName;

    private String guardianContact;

    public StudentRoaster(String studentId, String studentName, String studentGender, String homeTown, String guardianName, String guardianContact) {
        this.studentId = studentId;
        this.studentName = studentName;
        this.studentGender = studentGender;
        this.homeTown = homeTown;
        this.guardianName = guardianName;
        this.guardianContact = guardianContact;
    }

    public String getStudentId() {
        return studentId;
    }

    public String getStudentName() {
        return studentName;
    }

    public String getStudentGender() {
        return studentGender;
    }

    public String getHomeTown() {
        return homeTown;
    }

    public String getGuardianName() {
        return guardianName;
    }

    public String getGuardianContact() {
        return guardianContact;
    }
}
