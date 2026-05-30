package com.codewithben.schoolmanagementsystem.DTO.Academics;

public class ViewStudentsSubjectsResults {
    private String subjectName;

    private String classTest1Score;

    private String groupWorkScore;

    private String classTest2Score;

    private String projectScore;

    private String classScore;

    private String examScore;

    private String calculatedExamScore;

    private String total;

    private String grade;

    private String description;

    public ViewStudentsSubjectsResults(String subjectName, String classTest1Score, String groupWorkScore,
                                       String classTest2Score, String projectScore, String classScore,
                                       String examScore, String calculatedExamScore, String total,
                                       String grade, String description) {
        this.subjectName = subjectName;
        this.classTest1Score = classTest1Score;
        this.groupWorkScore = groupWorkScore;
        this.classTest2Score = classTest2Score;
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

    public String getClassTest1Score() {
        return classTest1Score;
    }

    public String getGroupWorkScore() {
        return groupWorkScore;
    }

    public String getClassTest2Score() {
        return classTest2Score;
    }

    public String getProjectScore() {
        return projectScore;
    }

    public String getClassScore() {
        return classScore;
    }

    public String getExamScore() {
        return examScore;
    }

    public String getCalculatedExamScore() {
        return calculatedExamScore;
    }

    public String getTotal() {
        return total;
    }

    public String getGrade() {
        return grade;
    }

    public String getDescription() {
        return description;
    }
}
