package com.codewithben.schoolmanagementsystem.DTO.Offline;

import com.codewithben.schoolmanagementsystem.DTO.Result.SaveStudentScores;
import com.codewithben.schoolmanagementsystem.DTO.Students.StudentsScoresTable;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class OfflineScoresList {

    private String subjectId;

    private String semesterId;

    private List<StudentsScoresTable> scores;
}
