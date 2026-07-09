package com.codewithben.schoolmanagementsystem.DTO.Students;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class StudentRoaster {
    private String studentId;

    private String studentName;

    private int presentCount;
}
