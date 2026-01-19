package com.codewithben.schoolmanagementsystem.DTO.Academics;

public class GradingCriteria {
    private Double lowerRange;

    private Double upperRange;

    private String grade;

    private String gradeDescription;

    private String staffId;

    public GradingCriteria(Double lowerRange, Double upperRange, String grade, String gradeDescription,
                           String staffId) {
        this.lowerRange = lowerRange;
        this.upperRange = upperRange;
        this.grade = grade;
        this.gradeDescription = gradeDescription;
        this.staffId = staffId;
    }

    public Double getLowerRange() {
        return lowerRange;
    }

    public void setLowerRange(Double lowerRange) {
        this.lowerRange = lowerRange;
    }

    public Double getUpperRange() {
        return upperRange;
    }

    public void setUpperRange(Double upperRange) {
        this.upperRange = upperRange;
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

    public void setGradeDescription(String gradeDescription) {
        this.gradeDescription = gradeDescription;
    }

    public String getStaffId() {
        return staffId;
    }

    public void setStaffId(String staffId) {
        this.staffId = staffId;
    }
}
