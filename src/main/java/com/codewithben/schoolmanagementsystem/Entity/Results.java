package com.codewithben.schoolmanagementsystem.Entity;

import jakarta.persistence.*;

import java.time.LocalDate;
import java.util.List;

@Entity
public class Results {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long resultId;

    @ManyToOne
    @JoinColumn(nullable = false, name = "student_studentId")
    private Students student;

    @ManyToOne
    @JoinColumn(nullable = false, name = "level_levelID")
    private Level level;

    @ManyToOne
    @JoinColumn(nullable = false, name = "semester_semesterID")
    private Semester semester;

    @OneToMany(mappedBy = "results", cascade = CascadeType.ALL)
    private List<SubjectScore> subjectScores;

    @OneToOne(mappedBy = "results")
    private Conduct conduct;

    private LocalDate createdAt;

    private LocalDate updatedAt;

    @ManyToOne
    @JoinColumn(nullable = false)
    private Staffs updatedBy;

    private Double totalScore;

    private Double averageScore;

    private String position;


    public long getResultId() {
        return resultId;
    }

    public void setResultId(long resultId) {
        this.resultId = resultId;
    }

    public Students getStudent() {
        return student;
    }

    public void setStudent(Students student) {
        this.student = student;
    }

    public Level getLevel() {
        return level;
    }

    public void setLevel(Level level) {
        this.level = level;
    }

    public Semester getSemester() {
        return semester;
    }

    public void setSemester(Semester semester) {
        this.semester = semester;
    }

    public List<SubjectScore> getSubjectScores() {
        return subjectScores;
    }

    public void setSubjectScores(List<SubjectScore> subjectScores) {
        this.subjectScores = subjectScores;
    }

    public LocalDate getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDate createdAt) {
        this.createdAt = createdAt;
    }

    public LocalDate getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(LocalDate updatedAt) {
        this.updatedAt = updatedAt;
    }

    public Staffs getUpdatedBy() {
        return updatedBy;
    }

    public void setUpdatedBy(Staffs updatedBy) {
        this.updatedBy = updatedBy;
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
}
