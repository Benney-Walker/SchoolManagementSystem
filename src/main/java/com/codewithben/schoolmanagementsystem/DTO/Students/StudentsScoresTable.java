package com.codewithben.schoolmanagementsystem.DTO.Students;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/*
 * One student's marks for a subject. Validated in ScoresService with ScoreRules, not with
 * Bean Validation annotations: those don't run for a List request body. classScore and
 * calculatedExamScore are returned when loading scores; on save they're ignored and
 * recomputed from the marks.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class StudentsScoresTable {
    @NotNull(message = "Student Id can not be empty")
    private String studentId;

    private String studentName;

    private String classTest1Score;

    private String classTest2Score;

    private String groupWorkScore;

    private String projectScore;

    private String classScore;

    private String examScore;

    private String calculatedExamScore;
}
