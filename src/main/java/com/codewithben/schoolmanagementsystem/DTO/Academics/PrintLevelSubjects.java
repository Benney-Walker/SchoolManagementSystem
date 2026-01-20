package com.codewithben.schoolmanagementsystem.DTO.Academics;

public class PrintLevelSubjects {
    private String subjectName;

    private String subjectId;

    public PrintLevelSubjects(String subjectName, String subjectId) {
        this.subjectName = subjectName;
        this.subjectId = subjectId;
    }

    public String getSubjectName() {
        return subjectName;
    }
    public void setSubjectName(String subjectName) {
        this.subjectName = subjectName;
    }

    public String getSubjectId() {
        return subjectId;
    }

    public void setSubjectId(String subjectId) {
        this.subjectId = subjectId;
    }
}
