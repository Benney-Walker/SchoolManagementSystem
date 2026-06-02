package com.codewithben.schoolmanagementsystem.DTO.Result;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class GradingCriteria {

    private int id;

    private Double lowerRange;

    private Double upperRange;

    private String grade;

    private String gradeDescription;
}
