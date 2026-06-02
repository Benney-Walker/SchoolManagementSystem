package com.codewithben.schoolmanagementsystem.DTO.Subject;

public class SubjectDTO {

    private String subjectId;

    private String subjectName;

    private String levelId;

    public SubjectDTO(String subjectId, String subjectName, String levelId) {
        this.subjectId = subjectId;
        this.subjectName = subjectName;
        this.levelId = levelId;
    }

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

    public String getLevelId() {
        return levelId;
    }

    public void setLevelId(String levelId) {
        this.levelId = levelId;
    }
}
