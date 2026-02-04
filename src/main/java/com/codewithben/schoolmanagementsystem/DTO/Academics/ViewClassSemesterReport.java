package com.codewithben.schoolmanagementsystem.DTO.Academics;

import java.util.List;

public class ViewClassSemesterReport {
    private String studentId;

    private String studentName;

    private String className;

    private String semesterName;

    private String total;

    private String average;

    private String classPosition;

    private String totalStudents;

    private List<ViewStudentsSubjectsResults> subjects;

    public ViewClassSemesterReport(String studentId, String studentName, String className, String semesterName,
                                   String classPosition, String total, String average, List<ViewStudentsSubjectsResults> subjects,
                                   String totalStudents) {
        this.studentId = studentId;
        this.studentName = studentName;
        this.className = className;
        this.semesterName = semesterName;
        this.subjects = subjects;
        this.classPosition = classPosition;
        this.total = total;
        this.average = average;
        this.classPosition = classPosition;
        this.totalStudents = totalStudents;
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

    public List<ViewStudentsSubjectsResults> getSubjects() {
        return subjects;
    }

    public void setSubjects(List<ViewStudentsSubjectsResults> subjects) {
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

    public String getTotalStudents() {
        return totalStudents;
    }

    public void setTotalStudents(String totalStudents) {
        this.totalStudents = totalStudents;
    }
}
