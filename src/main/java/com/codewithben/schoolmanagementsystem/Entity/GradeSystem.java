package com.codewithben.schoolmanagementsystem.Entity;

import jakarta.persistence.*;

@Entity
public class GradeSystem {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private Double lowerRange;

    @Column(nullable = false)
    private Double upperRange;

    @Column(nullable = false)
    private String grade;

    @Column(length = 50)
    private String gradeDescription;

    @ManyToOne(fetch = FetchType.LAZY)
    private Institution institution;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Double getLowerRange() {
        return lowerRange;
    }

    public void setLowerRange(Double gradeRange) {
        this.lowerRange = gradeRange;
    }

    public Double getUpperRange() {
        return upperRange;
    }

    public void setUpperRange(Double gradeRange) {
        this.upperRange = gradeRange;
    }

    public String getGrade() {
        return grade;
    }

    public void setGrade(String grade) {
        this.grade = grade;
    }

    public String getGradeDescription() {
        return gradeDescription;
    }

    public void setGradeDescription(String grade) {
        this.gradeDescription = grade;
    }

    public Institution getInstitution() {
        return institution;
    }

    public void setInstitution(Institution institution) {
        this.institution = institution;
    }
}
