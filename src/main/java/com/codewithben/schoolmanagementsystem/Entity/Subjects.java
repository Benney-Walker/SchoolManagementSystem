package com.codewithben.schoolmanagementsystem.Entity;

import jakarta.persistence.*;

import java.util.List;

@Entity
public class Subjects {

    @Id
    private String subjectId;

    private String subjectName;

    @OneToMany(mappedBy = "subject")
    private List<SubjectScore> subjectScore;

    @ManyToOne
    @JoinColumn(name = "Level_levelID")
    private Level level;

    @ManyToOne
    @JoinColumn(name = "Semester_semesterID")
    private Semester semester;

    public String getSubjectId() {
        return subjectId;
    }

    public void setSubjectId(String subjectId) {
        this.subjectId = subjectId;
    }

    public String getSubjectName() {
        return subjectName;
    }

    public void setSubjectName(String subjectName) {
        this.subjectName = subjectName;
    }

    public List<SubjectScore> getSubjectScore() {
        return subjectScore;
    }

    public void setSubjectScore(List<SubjectScore> subjectScore) {
        this.subjectScore = subjectScore;
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
}
