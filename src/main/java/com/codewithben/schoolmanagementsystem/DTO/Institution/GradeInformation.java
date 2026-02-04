package com.codewithben.schoolmanagementsystem.DTO.Institution;

import java.util.List;

public class GradeInformation {
    private String gradeId;

    private String gradeName;

    private String gradeSize;

    private List<StudentRoaster> studentRoasters;

    public GradeInformation(String gradeId, String gradeName,
                            String gradeSize, List<StudentRoaster> studentRoasters) {
        this.gradeId = gradeId;
        this.gradeName = gradeName;
        this.gradeSize = gradeSize;
        this.studentRoasters = studentRoasters;
    }

    public String getGradeId() {
        return gradeId;
    }

    public String getGradeName() {
        return gradeName;
    }

    public String getGradeSize() {
        return gradeSize;
    }

    public List<StudentRoaster> getStudentRoasters() {
        return studentRoasters;
    }
}
