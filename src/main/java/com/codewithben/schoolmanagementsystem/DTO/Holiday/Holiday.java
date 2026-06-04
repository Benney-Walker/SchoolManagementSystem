package com.codewithben.schoolmanagementsystem.DTO.Holiday;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Holiday {

    private int holidayId;

    private String holidayName;

    private String startDate;

    private String endDate;

    private String semesterId;

    private String semesterName;

    private String academicYear;
}
