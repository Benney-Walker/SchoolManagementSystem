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

    private Double classScore;

    private Double examScore;

    private Double totalScore;

    private String grade;

    private String remarks;

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public Subjects getSubject() {
        return subject;
    }

    public void setSubject(Subjects subjects) {
        this.subject = subjects;
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
