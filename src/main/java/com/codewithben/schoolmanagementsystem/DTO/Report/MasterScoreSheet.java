package com.codewithben.schoolmanagementsystem.DTO.Report;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MasterScoreSheet {

    private String className;

    private String semesterName;

    private String academicYear;

    private List<String> subjects;

    private List<MasterScoreSheetRow> records;
}
