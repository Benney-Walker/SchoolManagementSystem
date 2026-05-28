package com.codewithben.schoolmanagementsystem.Entity;

import jakarta.persistence.*;

@Entity
public class SubjectScore {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long id;

    @ManyToOne
    @JoinColumn(nullable = false, name = "Subject_subjectId")
    private Subjects subject;

    @ManyToOne
    @JoinColumn(nullable = false, name = "Student_studentId")
    private Students student;

    @ManyToOne
    @JoinColumn(name = "results_resultId")
    private Results results;

    //Now Project work
    private Double exercise1Score;

    //Now Group work
    private Double exercise2Score;

    //Now Class test 1
    private Double classTestScore;

    //Now Class test 2
    private Double projectScore;

    private Double classScore;

    private Double examScore;

    private Double calculatedExamScore;

    private Double totalScore;

    private String grade;

    private String remarks;

    public SubjectScore() {}

    public SubjectScore(long id, Subjects subject, Students student, Results results,
                        Double exercise1Score, Double classTestScore, Double exercise2Score,
                        Double projectScore, Double classScore, Double examScore, Double calculatedExamScore, Double totalScore, String grade, String remarks) {
        this.id = id;
        this.subject = subject;
        this.student = student;
        this.results = results;
        this.exercise1Score = exercise1Score;
        this.classTestScore = classTestScore;
        this.exercise2Score = exercise2Score;
        this.projectScore = projectScore;
        this.classScore = classScore;
        this.examScore = examScore;
        this.calculatedExamScore = calculatedExamScore;
        this.totalScore = totalScore;
        this.grade = grade;
        this.remarks = remarks;
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public Subjects getSubject() {
        return subject;
    }

    public void setSubject(Subjects subject) {
        this.subject = subject;
    }

    public Students getStudent() {
        return student;
    }

    public void setStudent(Students student) {
        this.student = student;
    }

    public Results getResults() {
        return results;
    }

    public void setResults(Results results) {
        this.results = results;
    }

    public Double getExercise1Score() {
        return exercise1Score;
    }

    public void setExercise1Score(Double exercise1Score) {
        this.exercise1Score = exercise1Score;
    }

    public Double getClassTestScore() {
        return classTestScore;
    }

    public void setClassTestScore(Double classTestScore) {
        this.classTestScore = classTestScore;
    }

    public Double getExercise2Score() {
        return exercise2Score;
    }

    public void setExercise2Score(Double exercise2Score) {
        this.exercise2Score = exercise2Score;
    }

    public Double getProjectScore() {
        return projectScore;
    }

    public void setProjectScore(Double projectScore) {
        this.projectScore = projectScore;
    }

    public Double getClassScore() {
        return classScore;
    }

    public void setClassScore(Double classScore) {
        this.classScore = classScore;
    }

    public Double getExamScore() {
        return examScore;
    }

    public void setExamScore(Double examScore) {
        this.examScore = examScore;
    }

    public Double getCalculatedExamScore() {
        return calculatedExamScore;
    }

    public void setCalculatedExamScore(Double calculatedExamScore) {
        this.calculatedExamScore = calculatedExamScore;
    }

    public Double getTotalScore() {
        return totalScore;
    }

    public void setTotalScore(Double totalScore) {
        this.totalScore = totalScore;
    }

    public String getGrade() {
        return grade;
    }

    public void setGrade(String grade) {
        this.grade = grade;
    }

    public String getRemarks() {
        return remarks;
    }

    public void setRemarks(String remarks) {
        this.remarks = remarks;
    }
}
