package com.codewithben.schoolmanagementsystem.DTO.Academics;

public class GradeInfoResponse {
    String gradeName;

    String gradeNumber;

    String gradeSize;

    public GradeInfoResponse(String gradeName, String gradeNumber, String gradeSize) {
        this.gradeName = gradeName;
        this.gradeNumber = gradeNumber;
        this.gradeSize = gradeSize;
    }

    public String getGradeName() {
        return gradeName;
    }

    public String getGradeNumber() {
        return gradeNumber;
    }

    public String getGradeSize() {
        return gradeSize;
    }
}
