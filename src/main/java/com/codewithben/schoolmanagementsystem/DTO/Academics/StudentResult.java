package com.codewithben.schoolmanagementsystem.DTO.Academics;

import java.util.List;

public class StudentResult {

    private String studentId;

    private String studentName;

    private String className;

    private String semesterName;

    private Double totalScore;

    private Double averageScore;

    private String position;

    private List<StudentSubjectResults>  studentSubjectResults;

    public StudentResult(String studentId, String studentName, String className, String semesterName, Double totalScore,
                         Double averageScore, String position, List<StudentSubjectResults> studentSubjectResults) {
        this.studentId = studentId;
        this.studentName = studentName;
        this.className = className;
        this.semesterName = semesterName;
        this.totalScore = totalScore;
        this.averageScore = averageScore;
        this.position = position;
        this.studentSubjectResults = studentSubjectResults;
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

    public Double getTotalScore() {
        return totalScore;
    }

    public void setTotalScore(Double totalScore) {
        this.totalScore = totalScore;
    }

    public Double getAverageScore() {
        return averageScore;
    }

    public void setAverageScore(Double averageScore) {
        this.averageScore = averageScore;
    }

    public String getPosition() {
        return position;
    }

    public void setPosition(String position) {
        this.position = position;
    }

    public List<StudentSubjectResults> getStudentSubjectResults() {
        return studentSubjectResults;
    }

    public void setStudentSubjectResults(List<StudentSubjectResults> studentSubjectResults) {
        this.studentSubjectResults = studentSubjectResults;
    }
}
