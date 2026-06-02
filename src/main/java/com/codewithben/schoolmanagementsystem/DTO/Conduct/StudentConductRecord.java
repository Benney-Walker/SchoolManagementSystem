package com.codewithben.schoolmanagementsystem.DTO.Conduct;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class StudentConductRecord {

    private String studentId;

    private String studentName;

    private String semesterId;

    // Six conduct categories, each holding a rating enum string (nullable).
    private String regular;

    private String punctual;

    private String physicalAppearance;

    private String social;

    private String emotional;

    private String cognitiveSkills;

    // Free text
    private String conductRemark;
}
