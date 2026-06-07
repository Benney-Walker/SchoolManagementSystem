package com.codewithben.schoolmanagementsystem.DTO.Report;

import com.codewithben.schoolmanagementsystem.DTO.Conduct.StudentConductReport;
import lombok.*;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class GenerateStudentResult {
    private String studentId;

    private String studentName;

    private String className;

    private String semesterName;

    private String totalScore;

    private String averageScore;

    private String classPosition;

    private String academicYear;

    private String totalStudents;

    private String vacationDate;

    private String resumptionDate;

    private String attendancePresent;

    private String totalAttendance;

    private String instructorName;

    private String currentDate;

    private String promotedTo;

    private List<SubjectReportDTO> subjectScores;

    private StudentConductReport studentConductReport;
}
