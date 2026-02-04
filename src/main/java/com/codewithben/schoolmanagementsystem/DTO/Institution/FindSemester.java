package com.codewithben.schoolmanagementsystem.DTO.Institution;

public class FindSemester {
    private String semesterID;

    private String semesterName;

    private String semesterStartDate;

    private String semesterEndDate;

    private String academicYear;

    public FindSemester() {}

    public FindSemester(String semesterID, String semesterName, String semesterStartDate, String semesterEndDate, String academicYear) {
        this.semesterID = semesterID;
        this.semesterName = semesterName;
        this.semesterStartDate = semesterStartDate;
        this.semesterEndDate = semesterEndDate;
        this.academicYear = academicYear;
    }

    public void setSemesterID(String semesterID) {
        this.semesterID = semesterID;
    }

    public String getSemesterID() {
        return semesterID;
    }

    public String getSemesterName() {
        return semesterName;
    }

    public void setSemesterName(String semesterName) {
        this.semesterName = semesterName;
    }

    public String getSemesterStartDate() {
        return semesterStartDate;
    }

    public void setSemesterStartDate(String semesterStartDate) {
        this.semesterStartDate = semesterStartDate;
    }

    public String getSemesterEndDate() {
        return semesterEndDate;
    }

    public void setSemesterEndDate(String semesterEndDate) {
        this.semesterEndDate = semesterEndDate;
    }

    public String getAcademicYear() {
        return academicYear;
    }

    public void setAcademicYear(String academicYear) {
        this.academicYear = academicYear;
    }
}
