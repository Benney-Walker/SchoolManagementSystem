package com.codewithben.schoolmanagementsystem.DTO.Result;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SbaRecords {

    private String studentId;

    private String studentName;

    private String classTest1Score;

    private String groupWorkScore;

    private String classTest2Score;

    private String projectScore;

    private String classScore;

    private String examScore;

    private String calculatedExamScore;
}
