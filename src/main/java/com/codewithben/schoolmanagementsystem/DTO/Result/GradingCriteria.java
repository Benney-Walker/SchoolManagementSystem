package com.codewithben.schoolmanagementsystem.DTO.Result;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;
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

    @NotNull(message = "Lower range is required")
    @PositiveOrZero(message = "Lower range must be zero or greater")
    private Double lowerRange;

    @NotNull(message = "Upper range is required")
    @PositiveOrZero(message = "Upper range must be zero or greater")
    private Double upperRange;

    @NotBlank(message = "Grade is required")
    private String grade;

    private String gradeDescription;
}
