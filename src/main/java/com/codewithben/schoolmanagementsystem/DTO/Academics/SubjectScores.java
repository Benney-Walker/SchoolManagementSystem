package com.codewithben.schoolmanagementsystem.DTO.Academics;

import java.util.List;

public class SubjectScores {
    private String studentId;

    private String studentName;

    private List<GetSubjectStudents> getSubjectStudents;

    public SubjectScores(String studentId, String studentName, List<GetSubjectStudents> getSubjectStudents) {
        this.studentId = studentId;
        this.studentName = studentName;
        this.getSubjectStudents = getSubjectStudents;
    }

    public String getStudentId() {
        return studentId;
    }

    public void setStudentId(String studentId) {
        this.studentId = studentId;
    }

    public String getStudentName() {
        return studentName;
    }

    public void setStudentName(String studentName) {
        this.studentName = studentName;
    }

    public List<GetSubjectStudents> getGetSubjectStudents() {
        return getSubjectStudents;
    }

    public void setGetSubjectStudents(List<GetSubjectStudents> getSubjectStudents) {
        this.getSubjectStudents = getSubjectStudents;
    }
}
