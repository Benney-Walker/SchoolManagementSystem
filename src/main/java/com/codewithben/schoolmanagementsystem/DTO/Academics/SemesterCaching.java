package com.codewithben.schoolmanagementsystem.DTO.Academics;

public class SemesterCaching {
    String semesterId;

    String semesterName;

    String academicYear;

    public SemesterCaching(String semesterId, String semesterName, String academicYear) {
        this.semesterId = semesterId;
        this.semesterName = semesterName;
        this.academicYear = academicYear;
    }

    public String getSemesterId() {
        return semesterId;
    }

    public void setSemesterId(String semesterId) {
        this.semesterId = semesterId;
    }

    public String getSemesterName() {
        return semesterName;
    }

    public void setSemesterName(String semesterName) {
        this.semesterName = semesterName;
    }

    public String getAcademicYear() {
        return academicYear;
    }

    public void setAcademicYear(String academicYear) {
        this.academicYear = academicYear;
    }
}
