package com.codewithben.schoolmanagementsystem.DTO.Students;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class StudentsScoresTable {
    @NotNull(message = "Student Id can not be empty")
    private String studentId;

    private String studentName;

    @Pattern(regexp = "^(30|[0-2]?[0-9])$", message = "Class Test 1 score must be between 0 and 30")
    private String classTest1Score;

    @Pattern(regexp = "^(30|[0-2]?[0-9])$", message = "Class Test 2 score must be between 0 and 30")
    private String classTest2Score;

    @Pattern(regexp = "^(20|1?[0-9])$", message = "Group Work score must be between 0 and 20")
    private String groupWorkScore;

    @Pattern(regexp = "^(20|1?[0-9])$", message = "Project score must be between 0 and 20")
    private String projectScore;

    @Pattern(regexp = "^(50|[0-4]?[0-9])$", message = "Class score must be between 0 and 50")
    private String classScore;

    @Pattern(regexp = "^(100|[1-9]?[0-9])$", message = "Exam score must be between 0 and 100")
    private String examScore;

    @Pattern(regexp = "^(50|[0-4]?[0-9])$", message = "Calculated exam score must be between 0 and 50")
    private String calculatedExamScore;
}
