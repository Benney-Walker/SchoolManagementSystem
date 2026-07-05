package com.codewithben.schoolmanagementsystem.DTO.Offline;

import com.codewithben.schoolmanagementsystem.DTO.Result.SaveStudentScores;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class OfflineScoresList {

    private String subjectId;

    private String semesterId;

    private SaveStudentScores scores;

    private String staffId;
}
