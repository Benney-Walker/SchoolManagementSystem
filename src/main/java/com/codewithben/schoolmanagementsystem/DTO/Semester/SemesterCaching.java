package com.codewithben.schoolmanagementsystem.DTO.Semester;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class SemesterCaching {
    String semesterId;

    String semesterName;

    String academicYear;
}
