package com.codewithben.schoolmanagementsystem.DTO.Academics;

public class SaveStudentScores {
    private String studentId;

    private String projectWork;

    private String classTest1;

    private String groupWork;

    private String classTest2;

    private String classScore;

    private String examScore;

    private String calculatedExamScore;

    public SaveStudentScores(String studentId, String projectWork, String classTest1,
                             String groupWork, String classTest2, String classScore, String examScore, String calculatedExamScore) {
        this.studentId = studentId;
        this.projectWork = projectWork;
        this.classTest1 = classTest1;
        this.groupWork = groupWork;
        this.classTest2 = classTest2;
        this.classScore = classScore;
        this.examScore = examScore;
        this.calculatedExamScore = calculatedExamScore;
    }

    public String getStudentId() {
        return studentId;
    }

    public void setStudentId(String studentId) {
        this.studentId = studentId;
    }

    public String getProjectWork() {
        return projectWork;
    }

    public void setProjectWork(String projectWork) {
        this.projectWork = projectWork;
    }

    public String getClassTest1() {
        return classTest1;
    }

    public void setClassTest1(String classTest1) {
        this.classTest1 = classTest1;
    }

    public String getGroupWork() {
        return groupWork;
    }

    public void setGroupWork(String groupWork) {
        this.groupWork = groupWork;
    }

    public String getClassTest2() {
        return classTest2;
    }

    public void setClassTest2(String classTest2) {
        this.classTest2 = classTest2;
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

    public String getCalculatedExamScore() {
        return calculatedExamScore;
    }

    public void setCalculatedExamScore(String calculatedExamScore) {
        this.calculatedExamScore = calculatedExamScore;
    }
}
