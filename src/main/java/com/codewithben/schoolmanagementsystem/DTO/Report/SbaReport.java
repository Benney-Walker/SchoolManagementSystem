package com.codewithben.schoolmanagementsystem.DTO.Report;

import com.codewithben.schoolmanagementsystem.DTO.Result.SbaRecords;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SbaReport {

    private String semesterName;

    private String academicYear;

    private String className;

    private String subjectName;

    private List<SbaRecords> sbaRecords;

    private String dateOfReport;
}
