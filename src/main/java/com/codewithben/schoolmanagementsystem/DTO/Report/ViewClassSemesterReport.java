package com.codewithben.schoolmanagementsystem.DTO.Report;

import com.codewithben.schoolmanagementsystem.DTO.Result.ViewStudentsSubjectsResults;

import java.util.List;

public class ViewClassSemesterReport {
    private String studentId;

    private String studentName;

    private String className;

    private String semesterName;

    private String totalScore;

    private String averageScore;

    private String classPosition;

    private String totalStudents;

    private String semesterRemark;

    private List<ViewStudentsSubjectsResults> scoresList;

    public ViewClassSemesterReport(String studentId, String studentName, String className, String semesterName,
                                   String classPosition, String totalScore, String averageScore, String semesterRemark,
                                   List<ViewStudentsSubjectsResults> scoresList, String totalStudents) {
        this.studentId = studentId;
        this.studentName = studentName;
        this.className = className;
        this.semesterName = semesterName;
        this.scoresList = scoresList;
        this.classPosition = classPosition;
        this.totalScore = totalScore;
        this.averageScore = averageScore;
        this.classPosition = classPosition;
        this.totalStudents = totalStudents;
        this.semesterRemark = semesterRemark;
    }

    public String getStudentId() {
        return studentId;
    }

    public String getStudentName() {
        return studentName;
    }

    public String getClassName() {
        return className;
    }

    public String getSemesterName() {
        return semesterName;
    }

    public String getTotalScore() {
        return totalScore;
    }

    public String getAverageScore() {
        return averageScore;
    }

    public String getClassPosition() {
        return classPosition;
    }

    public String getTotalStudents() {
        return totalStudents;
    }

    public String getSemesterRemark() {
        return semesterRemark;
    }

    public List<ViewStudentsSubjectsResults> getScoresList() {
        return scoresList;
    }
}
