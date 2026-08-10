package com.codewithben.schoolmanagementsystem.DTO.Students;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class StudentsHolder {
    private String studentId;

    private String studentName;

}
