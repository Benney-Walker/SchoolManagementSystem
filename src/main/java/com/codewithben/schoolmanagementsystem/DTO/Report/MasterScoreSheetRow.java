package com.codewithben.schoolmanagementsystem.DTO.Report;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.Map;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MasterScoreSheetRow {

    private String studentId;

    private String studentName;

    private List<String> subjects;

    private Map<String, Double> subjectScore;

    private Double studentTotalScore;

    private Double studentAverageScore;

    private Integer position;
}
