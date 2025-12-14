package com.codewithben.schoolmanagementsystem.DTO.Academics;

public class AddNewSubject {
    private String subjectName;

    private String semesterId;

    private String gradeId;

    public AddNewSubject(String subjectName, String semesterId, String gradeId) {
        this.subjectName = subjectName;
        this.semesterId = semesterId;
        this.gradeId = gradeId;
    }

    public String getSubjectName() {
        return subjectName;
    }

    public void setSubjectName(String subjectName) {
        this.subjectName = subjectName;
    }

    public String getSemesterId() {
        return semesterId;
    }

    public void setSemesterId(String semesterId) {
        this.semesterId = semesterId;
    }

    public String getGradeId() {
        return gradeId;
    }

    public void setGradeId(String gradeId) {
        this.gradeId = gradeId;
    }
}
