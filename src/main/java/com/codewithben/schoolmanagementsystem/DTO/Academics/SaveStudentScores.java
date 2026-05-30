package com.codewithben.schoolmanagementsystem.DTO.Academics;

public class SaveStudentScores {
    private String studentId;

    private String projectScore;

    private String classTest1Score;

    private String groupWorkScore;

    private String classTest2Score;

    private String classScore;

    private String examScore;

    private String calculatedExamScore;

    public SaveStudentScores(String studentId, String projectScore, String classTest1Score,
                             String groupWorkScore, String classTest2Score, String classScore, String examScore, String calculatedExamScore) {
        this.studentId = studentId;
        this.projectScore = projectScore;
        this.classTest1Score = classTest1Score;
        this.groupWorkScore = groupWorkScore;
        this.classTest2Score = classTest2Score;
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

    public String getProjectScore() {
        return projectScore;
    }

    public void setProjectScore(String projectScore) {
        this.projectScore = projectScore;
    }

    public String getClassTest1Score() {
        return classTest1Score;
    }

    public void setClassTest1Score(String classTest1Score) {
        this.classTest1Score = classTest1Score;
    }

    public String getGroupWorkScore() {
        return groupWorkScore;
    }

    public void setGroupWorkScore(String groupWorkScore) {
        this.groupWorkScore = groupWorkScore;
    }

    public String getClassTest2Score() {
        return classTest2Score;
    }

    public void setClassTest2Score(String classTest2Score) {
        this.classTest2Score = classTest2Score;
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
