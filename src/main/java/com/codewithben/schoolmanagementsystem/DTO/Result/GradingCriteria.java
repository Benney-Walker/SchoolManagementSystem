package com.codewithben.schoolmanagementsystem.DTO.Result;

public class GradingCriteria {
    private Double lowerRange;

    private Double upperRange;

    private String grade;

    private String gradeDescription;

    public GradingCriteria(Double lowerRange, Double upperRange, String grade, String gradeDescription) {
        this.lowerRange = lowerRange;
        this.upperRange = upperRange;
        this.grade = grade;
        this.gradeDescription = gradeDescription;
    }

    public Double getLowerRange() {
        return lowerRange;
    }

    public Double getUpperRange() {
        return upperRange;
    }

    public String getGrade() {
        return grade;
    }

    public String getGradeDescription() {
        return gradeDescription;
    }
}
