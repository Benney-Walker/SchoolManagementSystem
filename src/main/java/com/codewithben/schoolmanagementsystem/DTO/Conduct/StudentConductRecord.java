package com.codewithben.schoolmanagementsystem.DTO.Conduct;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class StudentConductRecord {

    @NotBlank(message = "Student ID is required")
    private String studentId;

    private String studentName;

    @NotBlank(message = "Semester is required")
    private String semesterId;

    // Six conduct categories, each holding a rating enum string (nullable).
    private String regular;

    private String punctual;

    private String physicalAppearance;

    private String social;

    private String emotional;

    private String cognitiveSkills;

    private String conductRemark;

    private String peculiarIssue;
}
