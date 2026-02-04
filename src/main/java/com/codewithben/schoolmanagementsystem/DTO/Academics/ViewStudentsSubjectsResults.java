package com.codewithben.schoolmanagementsystem.DTO.Academics;

public class ViewStudentsSubjectsResults {
    private String subjectName;

    private String exercise1Score;

    private String classTestScore;

    private String exercise2Score;

    private String projectScore;

    private String classScore;

    private String examScore;

    private String calculatedExamScore;

    private String total;

    private String grade;

    private String description;

    public ViewStudentsSubjectsResults() {}

    public ViewStudentsSubjectsResults(String subjectName, String exercise1Score, String classTestScore,
                                       String exercise2Score, String projectScore, String classScore,
                                       String examScore, String calculatedExamScore, String total, String grade, String description) {
        this.subjectName = subjectName;
        this.exercise1Score = exercise1Score;
        this.classTestScore = classTestScore;
        this.exercise2Score = exercise2Score;
        this.projectScore = projectScore;
        this.classScore = classScore;
        this.examScore = examScore;
        this.calculatedExamScore = calculatedExamScore;
        this.total = total;
        this.grade = grade;
        this.description = description;
    }

    public String getSubjectName() {
        return subjectName;
    }

    public void setSubjectName(String subjectName) {
        this.subjectName = subjectName;
    }

    public String getExercise1Score() {
        return exercise1Score;
    }

    public void setExercise1Score(String exercise1Score) {
        this.exercise1Score = exercise1Score;
    }

    public String getClassTestScore() {
        return classTestScore;
    }

    public void setClassTestScore(String classTestScore) {
        this.classTestScore = classTestScore;
    }

    public String getExercise2Score() {
        return exercise2Score;
    }

    public void setExercise2Score(String exercise2Score) {
        this.exercise2Score = exercise2Score;
    }

    public String getProjectScore() {
        return projectScore;
    }

    public void setProjectScore(String projectScore) {
        this.projectScore = projectScore;
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

    public String getTotal() {
        return total;
    }

    public void setTotal(String total) {
        this.total = total;
    }

    public String getGrade() {
        return grade;
    }

    public void setGrade(String grade) {
        this.grade = grade;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }
}
