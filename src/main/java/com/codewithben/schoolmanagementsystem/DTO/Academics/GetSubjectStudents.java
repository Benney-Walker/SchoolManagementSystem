package com.codewithben.schoolmanagementsystem.DTO.Academics;

public class GetSubjectStudents {
    private String studentId;

    private String studentName;

    private String classScore;

    private String examScore;

    public GetSubjectStudents(String studentId, String studentName, String classScore, String examScore) {
        this.studentId = studentId;
        this.studentName = studentName;
        this.classScore = classScore;
        this.examScore = examScore;
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

    public String getClassScore() {
        return classScore;
    }

    public void setClassScore(String classScore) {
        this.classScore = classScore;
    }

    public String getExamScore() {
        return examScore;
    }

    public void setExamScore(String examScore) {
        this.examScore = examScore;
    }
}
