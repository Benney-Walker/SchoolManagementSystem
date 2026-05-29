package com.codewithben.schoolmanagementsystem.Entity;

import com.codewithben.schoolmanagementsystem.Contants.ConductRatings;
import jakarta.persistence.*;


@Entity
public class Conduct {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(nullable = false, name = "Student_studentId")
    private Students student;

    @OneToOne
    @JoinColumn(name = "Results_resultId")
    private Results results;

    @Enumerated(EnumType.STRING)
    private ConductRatings regular;

    @Enumerated(EnumType.STRING)
    private ConductRatings punctual;

    @Enumerated(EnumType.STRING)
    private ConductRatings physicalAppearance;

    @Enumerated(EnumType.STRING)
    private ConductRatings social;

    @Enumerated(EnumType.STRING)
    private ConductRatings emotional;

    @Enumerated(EnumType.STRING)
    private ConductRatings cognitiveSkills;

    private String classTeacherRemark;

    public Conduct() {}

    public Conduct(Students student, Results results, ConductRatings regular, ConductRatings punctual, ConductRatings physicalAppearance, ConductRatings social, ConductRatings emotional, ConductRatings cognitiveSkills, String classTeacherRemark) {
        this.student = student;
        this.results = results;
        this.regular = regular;
        this.punctual = punctual;
        this.physicalAppearance = physicalAppearance;
        this.social = social;
        this.emotional = emotional;
        this.cognitiveSkills = cognitiveSkills;
        this.classTeacherRemark = classTeacherRemark;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
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

    public ConductRatings getRegular() {
        return regular;
    }

    public void setRegular(ConductRatings regular) {
        this.regular = regular;
    }

    public ConductRatings getPunctual() {
        return punctual;
    }

    public void setPunctual(ConductRatings punctual) {
        this.punctual = punctual;
    }

    public ConductRatings getPhysicalAppearance() {
        return physicalAppearance;
    }

    public void setPhysicalAppearance(ConductRatings physicalAppearance) {
        this.physicalAppearance = physicalAppearance;
    }

    public ConductRatings getSocial() {
        return social;
    }

    public void setSocial(ConductRatings social) {
        this.social = social;
    }

    public ConductRatings getEmotional() {
        return emotional;
    }

    public void setEmotional(ConductRatings emotional) {
        this.emotional = emotional;
    }

    public ConductRatings getCognitiveSkills() {
        return cognitiveSkills;
    }

    public void setCognitiveSkills(ConductRatings cognitiveSkills) {
        this.cognitiveSkills = cognitiveSkills;
    }

    public String getClassTeacherRemark() {
        return classTeacherRemark;
    }

    public void setClassTeacherRemark(String classTeacherRemark) {
        this.classTeacherRemark = classTeacherRemark;
    }
}
