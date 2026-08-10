package com.codewithben.schoolmanagementsystem.DTO.Semester;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class FindSemester {

    @NotBlank(message = "Semester ID is required")
    private String semesterID;

    @NotBlank(message = "Semester name is required")
    private String semesterName;

    @NotBlank(message = "Start date is required")
    private String semesterStartDate;

    @NotBlank(message = "End date is required")
    private String semesterEndDate;

    @NotBlank(message = "Academic year is required")
    private String academicYear;
}
