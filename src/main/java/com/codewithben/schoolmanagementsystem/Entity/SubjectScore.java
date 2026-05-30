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

    //Then exercise1Score
    @Column(name = "exercise1score")
    private Double projectWork;

    //Then exercise2Score
    @Column(name = "exercise2score")
    private Double groupWork;

    //Then classTestScore
    @Column(name = "class_test_score")
    private Double classTest1;

    //Then projectScore
    @Column(name = "project_score")
    private Double classTest2;

    private Double classScore;

    private Double examScore;

    private Double calculatedExamScore;

    private Double totalScore;

    private String grade;

    private String gradeDescriptor;

    public SubjectScore() {}

    public SubjectScore(long id, Subjects subject, Students student, Results results,
                        Double projectWork, Double classTest1, Double groupWork,
                        Double classTest2, Double classScore, Double examScore, Double calculatedExamScore, Double totalScore, String grade, String gradeDescriptor) {
        this.id = id;
        this.subject = subject;
        this.student = student;
        this.results = results;
        this.projectWork = projectWork;
        this.classTest1 = classTest1;
        this.groupWork = groupWork;
        this.classTest2 = classTest2;
        this.classScore = classScore;
        this.examScore = examScore;
        this.calculatedExamScore = calculatedExamScore;
        this.totalScore = totalScore;
        this.grade = grade;
        this.gradeDescriptor = gradeDescriptor;
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

    public Double getProjectWork() {
        return projectWork;
    }

    public void setProjectWork(Double exercise1Score) {
        this.projectWork = exercise1Score;
    }

    public Double getClassTest1() {
        return classTest1;
    }

    public void setClassTest1(Double classTestScore) {
        this.classTest1 = classTestScore;
    }

    public Double getGroupWork() {
        return groupWork;
    }

    public void setGroupWork(Double exercise2Score) {
        this.groupWork = exercise2Score;
    }

    public Double getClassTest2() {
        return classTest2;
    }

    public void setClassTest2(Double projectScore) {
        this.classTest2 = projectScore;
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

    public String getGradeDescriptor() {
        return gradeDescriptor;
    }

    public void setGradeDescriptor(String remarks) {
        this.gradeDescriptor = remarks;
    }
}
