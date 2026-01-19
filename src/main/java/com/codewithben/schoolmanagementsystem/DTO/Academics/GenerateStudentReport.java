package com.codewithben.schoolmanagementsystem.DTO.Academics;

import java.util.List;

public class GenerateStudentReport {
    private String studentId;

    private String studentName;

    private String className;

    private String semesterName;

    private String total;

    private String average;

    private String classPosition;

    private String academicYear;

    private String totalStudents;

    private String vacationDate;

    private String resumptionDate;

    private String attendanceMarked;

    private String totalAttendance;

    private String instructorName;

    private String currentDate;

    private String promotedTo;

    private List<SubjectReportDTO> subjects;

    public GenerateStudentReport(String studentId, String studentName, String className, String semesterName,
                                 String classPosition, String total, String average, List<SubjectReportDTO> subjects,
                                 String academicYear, String totalStudents, String vacationDate, String resumptionDate,
                                 String attendanceMarked, String totalAttendance, String instructorName, String currentDate,
                                 String promotedTo) {
        this.studentId = studentId;
        this.studentName = studentName;
        this.className = className;
        this.semesterName = semesterName;
        this.subjects = subjects;
        this.classPosition = classPosition;
        this.total = total;
        this.average = average;
        this.totalStudents = totalStudents;
        this.vacationDate = vacationDate;
        this.resumptionDate = resumptionDate;
        this.attendanceMarked = attendanceMarked;
        this.totalAttendance = totalAttendance;
        this.instructorName = instructorName;
        this.currentDate = currentDate;
        this.academicYear = academicYear;
        this.promotedTo = promotedTo;
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

    public String getClassName() {
        return className;
    }

    public void setClassName(String className) {
        this.className = className;
    }

    public String getSemesterName() {
        return semesterName;
    }

    public void setSemesterName(String semesterName) {
        this.semesterName = semesterName;
    }

    public List<SubjectReportDTO> getSubjects() {
        return subjects;
    }

    public void setSubjects(List<SubjectReportDTO> subjects) {
        this.subjects = subjects;
    }

    public String getTotal() {
        return total;
    }

    public void setTotal(String total) {
        this.total = total;
    }

    public String getAverage() {
        return average;
    }

    public void setAverage(String average) {
        this.average = average;
    }

    public String getClassPosition() {
        return classPosition;
    }

    public void setClassPosition(String classPosition) {
        this.classPosition = classPosition;
    }

    public String getAcademicYear() {
        return academicYear;
    }

    public void setAcademicYear(String academicYear) {
        this.academicYear = academicYear;
    }

    public String getTotalStudents() {
        return totalStudents;
    }

    public void setTotalStudents(String totalStudents) {
        this.totalStudents = totalStudents;
    }

    public String getVacationDate() {
        return vacationDate;
    }

    public void setVacationDate(String vacationDate) {
        this.vacationDate = vacationDate;
    }

    public String getResumptionDate() {
        return resumptionDate;
    }

    public void setResumptionDate(String resumptionDate) {
        this.resumptionDate = resumptionDate;
    }

    public String getAttendanceMarked() {
        return attendanceMarked;
    }

    public void setAttendanceMarked(String attendanceMarked) {
        this.attendanceMarked = attendanceMarked;
    }

    public String getTotalAttendance() {
        return totalAttendance;
    }

    public void setTotalAttendance(String totalAttendance) {
        this.totalAttendance = totalAttendance;
    }

    public String getInstructorName() {
        return instructorName;
    }

    public void setInstructorName(String instructorName) {
        this.instructorName = instructorName;
    }

    public String getCurrentDate() {
        return currentDate;
    }

    public void setCurrentDate(String currentDate) {
        this.currentDate = currentDate;
    }

    public String getPromotedTo() {
        return promotedTo;
    }

    public void setPromotedTo(String promotedTo) {
        this.promotedTo = promotedTo;
    }
}
