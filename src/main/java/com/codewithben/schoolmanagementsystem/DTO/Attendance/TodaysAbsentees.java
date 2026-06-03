package com.codewithben.schoolmanagementsystem.DTO.Attendance;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TodaysAbsentees {
    private String studentId;

    private String studentName;

    private String studentGrade;

    private String instructorName;

    private String instructorId;


}
