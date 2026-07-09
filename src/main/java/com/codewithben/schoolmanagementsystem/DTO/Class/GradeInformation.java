package com.codewithben.schoolmanagementsystem.DTO.Class;

import com.codewithben.schoolmanagementsystem.DTO.Students.StudentRoaster;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class GradeInformation {
    private String gradeId;

    private String gradeName;

    private String gradeSize;

    private List<StudentRoaster> studentRoasters;

}
