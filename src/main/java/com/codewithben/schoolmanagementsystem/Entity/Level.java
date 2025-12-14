package com.codewithben.schoolmanagementsystem.Entity;

import jakarta.persistence.*;

import java.util.List;

@Entity
public class Level {

    @Id
    private String levelID;

    private String levelName;

    @OneToOne(mappedBy = "level")
    private Staffs instructor;

    @ManyToOne
    @JoinColumn(nullable = false, name = "Institution_institutionId")
    private Institution institution;

    @OneToMany(mappedBy = "level")
    private List<Subjects> subjects;

    @OneToMany(mappedBy = "level")
    private List<Students> students;

    @OneToMany(mappedBy = "level")
    private List<Fees> fees;

    public String getLevelID() {

        return levelID;
    }

    public void setLevelID(String classId) {
        this.levelID = classId;
    }

    public String getLevelName() {

        return levelName;
    }

    public void setLevelName(String className) {

        this.levelName = className;
    }

    public Staffs getInstructor() {

        return instructor;
    }

    public void setInstructor(Staffs instructorId) {

        this.instructor = instructorId;
    }

    public Institution getInstitution() {
        return institution;
    }

    public void setInstitution(Institution institution) {
        this.institution = institution;
    }

    public List<Subjects> getSubjects() {
        return subjects;
    }

    public void setSubjects(List<Subjects> subjectId) {

        this.subjects = subjectId;
    }

    public List<Students> getStudents() {

        return students;
    }

    public void setStudents(List<Students> students) {

        this.students = students;
    }

    public List<Fees> getFees() {
        return fees;
    }

    public void setFees(List<Fees> fees) {
        this.fees = fees;
    }
}
