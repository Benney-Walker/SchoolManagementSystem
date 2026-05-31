package com.codewithben.schoolmanagementsystem.DTO.Academics;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SubjectReportDTO {
    private String subjectName;

    private String classScore;

    private String examScore;

    private String total;

    private String grade;

    private String gradeDescriptor;
}
