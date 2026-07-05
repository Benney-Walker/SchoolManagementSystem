package com.codewithben.schoolmanagementsystem.DTO.Result;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SaveStudentScores {
    private String studentId;

    private String projectScore;

    private String classTest1Score;

    private String groupWorkScore;

    private String classTest2Score;

    private String classScore;

    private String examScore;

    private String calculatedExamScore;
}
