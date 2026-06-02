package com.codewithben.schoolmanagementsystem.DTO.Report;

public class StudentSubjectReport {
    private String subjectName;

    private String score;

    private String grade;

    private String remark;

    public StudentSubjectReport(String subjectName, String score, String grade, String remark) {
        this.subjectName = subjectName;
        this.score = score;
        this.grade = grade;
        this.remark = remark;
    }

    public String getSubjectName() {
        return subjectName;
    }

    public void setSubjectName(String subjectName) {
        this.subjectName = subjectName;
    }

    public String getScore() {
        return score;
    }

    public void setScore(String score) {
        this.score = score;
    }

    public String getGrade() {
        return grade;
    }

    public void setGrade(String grade) {
        this.grade = grade;
    }

    public String getRemark() {
        return remark;
    }

    public void setRemark(String remark) {
        this.remark = remark;
    }
}
