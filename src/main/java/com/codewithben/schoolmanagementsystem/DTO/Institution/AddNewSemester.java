package com.codewithben.schoolmanagementsystem.DTO.Institution;

public class AddNewSemester {
    private String semesterName;

    private String startDate;

    private String endDate;

    private String academicYear;

    public AddNewSemester(String semesterName, String startDate, String endDate, String academicYear) {
        this.semesterName = semesterName;
        this.startDate = startDate;
        this.endDate = endDate;
        this.academicYear = academicYear;
    }

    public String getSemesterName() {
        return semesterName;
    }

    public void setSemesterName(String semesterName) {
        this.semesterName = semesterName;
    }

    public String getStartDate() {
        return startDate;
    }

    public void setStartDate(String startDate) {
        this.startDate = startDate;
    }

    public String getEndDate() {
        return endDate;
    }

    public void setEndDate(String endDate) {
        this.endDate = endDate;
    }

    public String getAcademicYear() {
        return academicYear;
    }

    public void setAcademicYear(String academicYear) {
        this.academicYear = academicYear;
    }
}
