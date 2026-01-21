package com.codewithben.schoolmanagementsystem.DTO.Academics;

public class GradeInfoResponse {
    String gradeName;

    String gradeNumber;

    String gradeSize;

    public GradeInfoResponse() {}

    public GradeInfoResponse(String gradeName, String gradeNumber, String gradeSize) {
        this.gradeName = gradeName;
        this.gradeNumber = gradeNumber;
        this.gradeSize = gradeSize;
    }

    public String getGradeName() {
        return gradeName;
    }

    public void setGradeName(String gradeName) {
        this.gradeName = gradeName;
    }

    public String getGradeNumber() {
        return gradeNumber;
    }

    public void setGradeNumber(String gradeNumber) {
        this.gradeNumber = gradeNumber;
    }

    public String getGradeSize() {
        return gradeSize;
    }

    public void setGradeSize(String gradeSize) {
        this.gradeSize = gradeSize;
    }
}
